package br.com.zupacademy.valteir.pix.remover

import br.com.zupacademy.valteir.RemoveChavePixRequest
import br.com.zupacademy.valteir.RemovePixServiceGrpc
import br.com.zupacademy.valteir.TipoConta
import br.com.zupacademy.valteir.outros_sistemas.BancoCentralClient
import br.com.zupacademy.valteir.outros_sistemas.DeletePixKeyRequest
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
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpResponseFactory
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import java.util.*
import javax.inject.Inject

@MicronautTest(transactional = false)
internal class RemoveChavePixEndpointTest(
    val grpcClient: RemovePixServiceGrpc.RemovePixServiceBlockingStub,
    val repository: ChavePixRepository,
) {


    lateinit var CHAVE_EXISTENTE: ChavePix

    @Inject
    lateinit var bcbClient: BancoCentralClient

    @BeforeEach
    fun setup() {
        CHAVE_EXISTENTE = repository.save(ChavePix(
            UUID.randomUUID(),
            TipoChave.EMAIL,
            "teste@email.com",
            TipoConta.CONTA_CORRENTE,
            "60701190"
        ))
    }

    @AfterEach
    fun cleanUp() {
        repository.deleteAll()
    }

    @Test
    fun `deve remover chave pix`() {
        // cenario
        `when`(bcbClient.deletar(CHAVE_EXISTENTE.valor, DeletePixKeyRequest(CHAVE_EXISTENTE.valor, "60701190")))
            .thenReturn(HttpResponse.ok(Any()))

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
    fun `nao deve remover chave pix quando ocorrer um erro no servico do BCB`() {
        //cenario
        `when`(bcbClient.deletar(CHAVE_EXISTENTE.valor, DeletePixKeyRequest(CHAVE_EXISTENTE.valor, CHAVE_EXISTENTE.instituicao)))
            .thenThrow(HttpClientResponseException("falhou", HttpResponse.status<Any>(HttpStatus.UNPROCESSABLE_ENTITY)))

        //acao
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.remove(RemoveChavePixRequest.newBuilder()
                .setIdTitular(CHAVE_EXISTENTE.idTitular.toString())
                .setPixId(CHAVE_EXISTENTE.id.toString())
                .build()
            )
        }

        //validacao
        with(error) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("Erro ao remover a chave Pix do Banco Central do Brasil", status.description)
            assertTrue(repository.count() == 1L)
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



    @MockBean(BancoCentralClient::class)
    fun bcbClient() : BancoCentralClient {
        return Mockito.mock(BancoCentralClient::class.java)
    }

    @Factory
    class Clients {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel) : RemovePixServiceGrpc.RemovePixServiceBlockingStub? {
            return RemovePixServiceGrpc.newBlockingStub(channel)
        }
    }

}