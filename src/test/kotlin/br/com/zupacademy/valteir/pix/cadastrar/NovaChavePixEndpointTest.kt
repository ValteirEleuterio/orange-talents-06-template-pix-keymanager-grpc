package br.com.zupacademy.valteir.pix.cadastrar

import br.com.zupacademy.valteir.PixRequest
import br.com.zupacademy.valteir.PixServiceGrpc
import br.com.zupacademy.valteir.TipoChave
import br.com.zupacademy.valteir.TipoConta
import br.com.zupacademy.valteir.outros_sistemas.ContaResponse
import br.com.zupacademy.valteir.outros_sistemas.ItauClient
import br.com.zupacademy.valteir.pix.ChavePix
import br.com.zupacademy.valteir.pix.ChavePixRepository
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
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.any
import java.util.*
import javax.inject.Inject


@MicronautTest(transactional = false)
internal class NovaChavePixEndpointTest(
    val grpcClient: PixServiceGrpc.PixServiceBlockingStub,
    val repository: ChavePixRepository
) {

    @Inject
    lateinit var itauClient: ItauClient

    @BeforeEach
    fun setup() {
        repository.deleteAll()
    }

    @Test
    fun `deve cadastrar nova chave pix`() {
        val idTitular = "c56dfef4-7901-44fb-84e2-a2cefb157890"
        val request = PixRequest.newBuilder()
            .setIdTitular(idTitular)
            .setTipo(TipoChave.EMAIL)
            .setValorChave("valteir@hotmail.com")
            .setConta(TipoConta.CONTA_CORRENTE)
            .build()

        `when`(itauClient.consultaContaCliente(idTitular, TipoConta.CONTA_CORRENTE.toString()))
            .thenReturn(HttpResponse.ok())


        val response = grpcClient.cadastrar(request)

        with(response) {
            assertNotNull(pixId)
            assertTrue(repository.existsById(UUID.fromString(pixId)))
        }
    }


    @Test
    fun `nao deve cadastrar nova chave pix quando ja existente`() {

        // cenario
        val chavePixExistente = ChavePix(
            UUID.fromString("c56dfef4-7901-44fb-84e2-a2cefb157890"),
            br.com.zupacademy.valteir.pix.TipoChave.EMAIL,
            "valteir@hotmail.com",
            TipoConta.CONTA_CORRENTE
        )
        repository.save(chavePixExistente)

        // acao

        val novaChavePix = PixRequest.newBuilder()
            .setIdTitular(chavePixExistente.idTitular.toString())
            .setTipo(TipoChave.valueOf(chavePixExistente.tipo.toString()))
            .setValorChave(chavePixExistente.valor)
            .setConta(chavePixExistente.conta)
            .build()

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.cadastrar(novaChavePix)
        }


        // validacao

        with(error) {
            assertEquals(Status.ALREADY_EXISTS.code, status.code)
            assertEquals("Chave pix ${novaChavePix.valorChave} já está cadastrada", status.description)
            assertTrue(repository.count() == 1L)
        }
    }

    @Test
    fun `nao deve cadastrar chave pix quando cliente nao encontrado`() {
        //cenario
        `when`(itauClient.consultaContaCliente("c56dfef4-7901-44fb-84e2-a2cefb157890", tipo = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.notFound())

        //acao
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.cadastrar(
                PixRequest.newBuilder()
                    .setIdTitular("c56dfef4-7901-44fb-84e2-a2cefb157890")
                    .setTipo(TipoChave.EMAIL)
                    .setValorChave("valteir@hotmail.com")
                    .setConta(TipoConta.CONTA_CORRENTE)
                    .build()
            )
        }

        //validacao
        with(error) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("Cliente não encontrado no itau", status.description)
        }
    }

    @Test
    fun `nao deve cadastrar chave pix quando parametros invalidos`() {
        //acao
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.cadastrar(PixRequest.newBuilder().build())
        }

        //validacao
        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Dados inválidos", status.description)


            val details = StatusProto.fromThrowable(this)?.detailsList?.get(0)!!.unpack(BadRequest::class.java)
            val violations = details.fieldViolationsList.map { it.field to it.description }
            assertThat(
                violations, containsInAnyOrder(
                    "idTitular" to "não deve estar em branco",
                    "idTitular" to "não é um formato válido de UUID",
                    "tipoConta" to "não deve ser nulo",
                    "tipoChave" to "não deve ser nulo"
                )
            )
        }
    }


    @Test
    fun `nao deve registrar nova chave pix quando o valor da chave nao for compativel com o tipo`() {

        //acao
        val pixRequest = PixRequest.newBuilder()
            .setIdTitular(UUID.randomUUID().toString())
            .setTipo(TipoChave.CPF)
            .setValorChave("111.111.cpf-invalido.999")
            .setConta(TipoConta.CONTA_CORRENTE)
            .build()

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.cadastrar(pixRequest)
        }

        //validacao

        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Dados inválidos", status.description)

            val details = StatusProto.fromThrowable(this)?.detailsList?.get(0)!!.unpack(BadRequest::class.java)
            val violations = details.fieldViolationsList.map { it.field to it.description }
            assertThat(violations, containsInAnyOrder(
                "chave" to "chave Pix inválida (${pixRequest.tipo})"
            ))
        }
    }


    @MockBean(ItauClient::class)
    fun itauClient(): ItauClient? {
        return Mockito.mock(ItauClient::class.java)
    }

    @Factory
    class Clients {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): PixServiceGrpc.PixServiceBlockingStub? {
            return PixServiceGrpc.newBlockingStub(channel)
        }
    }

}
