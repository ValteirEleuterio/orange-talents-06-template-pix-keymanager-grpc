package br.com.zupacademy.valteir.pix.listar

import br.com.zupacademy.valteir.ListaChavesPixRequest
import br.com.zupacademy.valteir.ListaPixServiceGrpc
import br.com.zupacademy.valteir.TipoConta
import br.com.zupacademy.valteir.pix.ChavePix
import br.com.zupacademy.valteir.pix.ChavePixRepository
import br.com.zupacademy.valteir.pix.TipoChave
import br.com.zupacademy.valteir.pix.consultar.ConsultaChavePixEndpointTest
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.stub.AbstractBlockingStub
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
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
internal class ListarChavesPixEndpointTest(
    val repository: ChavePixRepository,
    val grpcClient: ListaPixServiceGrpc.ListaPixServiceBlockingStub
) {

    companion object {
        val CLIENTE_ID: UUID = UUID.randomUUID();
    }

    @BeforeEach
    fun setup() {
        repository.save(chave(tipo = TipoChave.EMAIL, chave = "teste@email.com", idTitular = CLIENTE_ID))
        repository.save(chave(tipo = TipoChave.CPF, chave = "11111111111", idTitular = CLIENTE_ID))
        repository.save(chave(tipo = TipoChave.ALEATORIA, chave = "aleatoria", idTitular = CLIENTE_ID))
        repository.save(chave(tipo = TipoChave.CELULAR, chave = "+5544998609170", idTitular = UUID.randomUUID()))
    }

    @AfterEach
    fun cleanUp() {
        repository.deleteAll()
    }

    @Test
    fun `deve listar as chaves do cliente`() {
        //cenario
        val listaChaves = repository.findByIdTitular(CLIENTE_ID)

        //acao
        val response = grpcClient.lista(
            ListaChavesPixRequest.newBuilder()
                .setIdTitular(CLIENTE_ID.toString())
                .build()
        )

        //validacao
        with(response.chavesList) {
            assertEquals(listaChaves.size, size)

            assertThat(this.map { it.tipo to it.chave }.toList(), containsInAnyOrder(
                br.com.zupacademy.valteir.TipoChave.EMAIL to "teste@email.com",
                br.com.zupacademy.valteir.TipoChave.CPF to "11111111111",
                br.com.zupacademy.valteir.TipoChave.ALEATORIA to "aleatoria"
            ) )
        }
    }

    @Test
    fun `nao deve listar as chaves quando idTitular for invalido`() {
        //acao
        val error  = assertThrows<StatusRuntimeException> {
            grpcClient.lista(ListaChavesPixRequest.newBuilder()
                .setIdTitular("")
                .build()
            )
        }
        
        //validacao
        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("idTitular deve ser preenchido", status.description)
        }
    }

    private fun chave(tipo: TipoChave, chave: String, idTitular: UUID): ChavePix {
        return ChavePix(
            idTitular,
            tipo,
            chave,
            TipoConta.CONTA_CORRENTE,
            "60701190"
        )
    }


    @Factory
    class Clients {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): ListaPixServiceGrpc.ListaPixServiceBlockingStub {
            return ListaPixServiceGrpc.newBlockingStub(channel)
        }
    }
}