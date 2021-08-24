package br.com.zupacademy.valteir.pix.remover

import br.com.zupacademy.valteir.RemoveChavePixRequest
import br.com.zupacademy.valteir.RemovePixServiceGrpc
import br.com.zupacademy.valteir.TipoConta
import br.com.zupacademy.valteir.pix.ChavePix
import br.com.zupacademy.valteir.pix.ChavePixRepository
import br.com.zupacademy.valteir.pix.TipoChave
import com.google.rpc.BadRequest
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.protobuf.StatusProto
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.annotation.GrpcService
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

@MicronautTest(transactional = false)
internal class RemoveChavePixEndpointTest(
    val grpcClient: RemovePixServiceGrpc.RemovePixServiceBlockingStub,
    val repository: ChavePixRepository
) {


    lateinit var CHAVE_EXISTENTE: ChavePix

    @BeforeEach
    fun setup() {
        CHAVE_EXISTENTE = repository.save(ChavePix(
            UUID.randomUUID(),
            TipoChave.EMAIL,
            "teste@email.com",
            TipoConta.CONTA_CORRENTE
        ))
    }

    @AfterEach
    fun cleanUp() {
        repository.deleteAll()
    }

    @Test
    fun `deve remover chave pix`() {
        // acao
        val response = grpcClient.remove(RemoveChavePixRequest.newBuilder()
            .setIdTitular(CHAVE_EXISTENTE.idTitular.toString())
            .setPixId(CHAVE_EXISTENTE.id.toString())
            .build()
        )

        // validacao
        with(response) {
            assertNotNull(this)
            assertEquals(CHAVE_EXISTENTE.idTitular.toString(), idTitular)
            assertEquals(CHAVE_EXISTENTE.id.toString(), pixId)
            assertTrue(repository.count() == 0L)
        }
    }

    @Test
    fun `nao deve remover chave pix quando nao existe`() {
        //cenario
        val chavePixIdNaoExistente = UUID.randomUUID()

        //acao
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.remove(RemoveChavePixRequest.newBuilder()
                .setPixId(chavePixIdNaoExistente.toString())
                .setIdTitular(CHAVE_EXISTENTE.idTitular.toString())
                .build()
            )
        }

        with(error) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave pix: ${chavePixIdNaoExistente} não encontrada", status.description)
            assertTrue(repository.count() == 1L)
        }



        //validacao
    }

    @Test
    fun `nao deve remover chave pix quando chave existente mas pertence a outro cliente`() {
        //cenario
        val outroTitularId = UUID.randomUUID().toString()

        //acao
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.remove(RemoveChavePixRequest.newBuilder()
                .setIdTitular(outroTitularId)
                .setPixId(CHAVE_EXISTENTE.id.toString())
                .build()
            )
        }

        //validacao
        with(error) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("Chave pix não pertence ao titular de id: $outroTitularId", status.description)
            assertTrue(repository.count() == 1L)
        }
    }

    @Test
    fun `nao deve remover chave pix quando parametros invalidos`() {
        //acao
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.remove(RemoveChavePixRequest.newBuilder().build())
        }

        //validacao

        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Dados inválidos", status.description)
            assertTrue(repository.count() == 1L)
            val details = StatusProto.fromThrowable(this)?.detailsList?.get(0)!!.unpack(BadRequest::class.java)
            val violations = details.fieldViolationsList.map { it.field to it.description }

            assertThat(violations, containsInAnyOrder(
                "pixId" to "não deve estar em branco",
                "pixId" to "não é um formato válido de UUID",
                "idTitular" to "não deve estar em branco",
                "idTitular" to "não é um formato válido de UUID"
            ))
        }
    }



    @Factory
    class Clients {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel) : RemovePixServiceGrpc.RemovePixServiceBlockingStub? {
            return RemovePixServiceGrpc.newBlockingStub(channel)
        }
    }

}