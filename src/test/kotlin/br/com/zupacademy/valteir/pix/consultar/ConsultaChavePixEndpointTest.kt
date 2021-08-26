package br.com.zupacademy.valteir.pix.consultar

import br.com.zupacademy.valteir.ConsultaChavePixRequest
import br.com.zupacademy.valteir.ConsultaPixServiceGrpc
import br.com.zupacademy.valteir.TipoConta
import br.com.zupacademy.valteir.outros_sistemas.*
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
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

@MicronautTest(transactional = false)
internal class ConsultaChavePixEndpointTest(
    private val grpcClient: ConsultaPixServiceGrpc.ConsultaPixServiceBlockingStub,
    private val repository: ChavePixRepository,
) {

    @Inject
    lateinit var itauClient: ItauClient

    @Inject
    lateinit var bcbClient: BancoCentralClient

    companion object {
        val CLIENT_ID = UUID.randomUUID()
    }

    @BeforeEach
    fun setup() {
        repository.save(chave(tipo = TipoChave.EMAIL, chave = "teste@email.com", idTitular = CLIENT_ID))
        repository.save(chave(tipo = TipoChave.CPF, chave = "11111111111", idTitular = CLIENT_ID))
        repository.save(chave(tipo = TipoChave.ALEATORIA, chave = "aleatoria", idTitular = CLIENT_ID))
        repository.save(chave(tipo = TipoChave.CELULAR, chave = "+5544998609170", idTitular = CLIENT_ID))
    }


    @AfterEach
    fun cleanUp() {
        repository.deleteAll()
    }


    @Test
    fun `deve consultar uma chave pix existente quando buscar por chave presente no sistema`() {
        //cenario
        val chaveExistente = repository.findByValor("teste@email.com").get()

        `when`(itauClient.consultaContaCliente(CLIENT_ID.toString(), TipoConta.CONTA_CORRENTE.toString()))
            .thenReturn(HttpResponse.ok(dadosDaContaResponse()))

        //acao
        val response = grpcClient.consulta(ConsultaChavePixRequest.newBuilder()
            .setChave("teste@email.com")
            .build()
        )

        //validacao
        with(response) {
            assertEquals(chaveExistente.valor, chave.chave)
            assertEquals(chaveExistente.idTitular.toString(), clientId)
            assertEquals(chaveExistente.tipo.name, chave.tipo.name)
            assertEquals(chaveExistente.id.toString(), pixId)
        }
    }

    @Test
    fun `deve consultar uma chave pix existente no BCB quando nao esta presente no sistema`() {
        //cenario
        val chaveExistenteApenasNoBCB = "naoexiste@email.com"
        `when`(bcbClient.buscar(chaveExistenteApenasNoBCB)).thenReturn(HttpResponse.ok(dadosPixBCB()))

        //acao
        val response = grpcClient.consulta(ConsultaChavePixRequest.newBuilder()
            .setChave(chaveExistenteApenasNoBCB)
            .build()
        )

        //validacao
        with(response) {
            assertEquals(chaveExistenteApenasNoBCB, chave.chave)
        }
    }

    @Test
    fun `nao deve consultar por chave quando filtro invalido`() {
        //acao
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.consulta(ConsultaChavePixRequest.newBuilder()
                .setChave("")
                .build()
            )
        }

        //validacao
        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Dados inválidos", status.description)

            val details = StatusProto.fromThrowable(this)?.detailsList?.get(0)!!.unpack(BadRequest::class.java)
            val violations = details.fieldViolationsList.map { it.field to it.description }
            assertThat(violations, containsInAnyOrder(
                "chave" to "não deve estar em branco"
            ))
        }
    }

    @Test
    fun `nao deve consultar chave pix existente quando buscar por chave existente mas falhar ao buscar dados da conta no ERP ITAU`() {
        //cenario
        `when`(itauClient.consultaContaCliente(CLIENT_ID.toString(), TipoConta.CONTA_CORRENTE.toString()))
            .thenThrow(HttpClientResponseException("", HttpResponse.serverError<Any>()))

        //acao
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.consulta(ConsultaChavePixRequest.newBuilder()
                .setChave("teste@email.com")
                .build()
            )
        }

        //validacao
        with(error) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("Falha ao buscar dados da conta do cliente no ERP ITAU", status.description)
        }
    }

    @Test
    fun `nao deve consultar chave pix existente quando buscar por chave existente mas nao encontrar dados da conta no ERP ITAU`() {
        //cenario
        `when`(itauClient.consultaContaCliente(CLIENT_ID.toString(), TipoConta.CONTA_CORRENTE.toString()))
            .thenReturn(HttpResponse.notFound())

        //acao
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.consulta(ConsultaChavePixRequest.newBuilder()
                .setChave("teste@email.com")
                .build()
            )
        }

        //validacao
        with(error) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("Cliente nao encontrado no ERP ITAU", status.description)
        }
    }

    @Test
    fun `nao deve consultar chave pix quando buscar por chave que nao existe localmente nem no BCB`() {
        //cenario
        val chavePixNaoExistente = "naoexiste@email.com"
        `when`(bcbClient.buscar(chavePixNaoExistente))
            .thenReturn(HttpResponse.notFound())

        //acao
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.consulta(ConsultaChavePixRequest.newBuilder()
                .setChave(chavePixNaoExistente)
                .build()
            )
        }

        //validacao
        with(error){
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave Pix não encontrada", status.description)
        }
    }

    @Test
    fun `deve consultar chave pix existente quando buscar por pixId e idTitular presente no sistema`()  {
        //cenario
        val chaveExistente = repository.findByValor("teste@email.com").get()
        `when`(itauClient.consultaContaCliente(chaveExistente.idTitular.toString(), chaveExistente.conta.toString()))
            .thenReturn(HttpResponse.ok(dadosDaContaResponse()))

        //acao
        val response = grpcClient.consulta(ConsultaChavePixRequest.newBuilder()
            .setPixId(ConsultaChavePixRequest.FiltroPorPixId.newBuilder()
                .setPixId(chaveExistente.id.toString())
                .setIdTitular(chaveExistente.idTitular.toString())
                .build()
            )
            .build()
        )

        //validacao
        with(response){
            assertEquals(chaveExistente.id.toString(), pixId)
            assertEquals(chaveExistente.idTitular.toString(), clientId.toString())
            assertEquals(chaveExistente.valor, chave.chave)
            assertEquals(chaveExistente.tipo.name, chave.tipo.name)
        }
    }

    @Test
    fun `nao deve consultar por pixId e idTitular quando registro nao existir`() {
        // cenario
        val pixNaoExistente = UUID.randomUUID().toString()
        val titularNaoExistente = UUID.randomUUID().toString()

        // acao
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.consulta(ConsultaChavePixRequest.newBuilder()
                .setPixId(ConsultaChavePixRequest.FiltroPorPixId.newBuilder()
                    .setPixId(pixNaoExistente)
                    .setIdTitular(titularNaoExistente)
                )
                .build()
            )
        }

        //validacao
        with(error) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave Pix não encontrada", status.description)
        }
    }

    @Test
    fun `nao deve consultar por pixId e idTitular quando falhar ao buscar dados da conta no ERP ITAU`() {
        // cenario
        val chaveExistente = repository.findByValor("teste@email.com").get()
        `when`(itauClient.consultaContaCliente(chaveExistente.idTitular.toString(), chaveExistente.conta.toString()))
            .thenThrow(HttpClientResponseException("falhou", HttpResponse.serverError<Any>()))

        // acao
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.consulta(ConsultaChavePixRequest.newBuilder()
                .setPixId(ConsultaChavePixRequest.FiltroPorPixId.newBuilder()
                    .setIdTitular(chaveExistente.idTitular.toString())
                    .setPixId(chaveExistente.id.toString())
                    .build()
                )
                .build()
            )
        }

        // validacao
        with(error) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("Falha ao buscar dados da conta do cliente no ERP ITAU", status.description)
        }
    }

    @Test
    fun `nao deve consultar por pixId e idTitular quando nao encontrar os dados da conta no ERP ITAU`() {
        //cenario
        val chavePixExistente = repository.findByValor("teste@email.com").get()
        `when`(itauClient.consultaContaCliente(chavePixExistente.idTitular.toString(), chavePixExistente.conta.toString()))
            .thenReturn(HttpResponse.notFound())

        //acao
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.consulta(ConsultaChavePixRequest.newBuilder()
                .setPixId(ConsultaChavePixRequest.FiltroPorPixId.newBuilder()
                    .setPixId(chavePixExistente.id.toString())
                    .setIdTitular(chavePixExistente.idTitular.toString())
                )
                .build()
            )
        }

        //validacao
        with(error) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("Cliente nao encontrado no ERP ITAU", status.description)
        }
    }

    @Test
    fun `nao deve consultar por pixId e idTitular quando filtro invalido`() {
        //acao
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.consulta(ConsultaChavePixRequest.newBuilder()
                .setPixId(ConsultaChavePixRequest.FiltroPorPixId.newBuilder()
                        .setIdTitular("")
                        .setPixId("")
                        .build()
                )
                .build()
            )
        }

        //validacao
        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Dados inválidos", status.description)

            val details = StatusProto.fromThrowable(this)?.detailsList?.get(0)!!.unpack(BadRequest::class.java)
            val violations = details.fieldViolationsList.map { it.field to it.description }
            assertThat(violations, containsInAnyOrder(
                "idTitular" to "não deve estar em branco",
                "pixId" to "não deve estar em branco",
                "idTitular" to "não é um formato válido de UUID",
                "pixId" to "não é um formato válido de UUID"
            ))
        }
    }

    @Test
    fun `nao deve consultar chave quando filtro invalido`() {
        //acao
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.consulta(ConsultaChavePixRequest.newBuilder().build())
        }

        //validacao
        with(error){
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Chave Pix inválida ou não informada", status.description)
        }
    }


    private fun dadosPixBCB(): PixKeyDetailsResponse {
        return PixKeyDetailsResponse(
            PixKeyType.EMAIL,
            "naoexiste@email.com",
            BankAccount(
                "60701190",
                "1323",
            "999564",
                BankAccount.AccountType.CACC
            ),
            Owner("Testando", "123"),
            LocalDateTime.now()
        )
    }


    private fun dadosDaContaResponse(): ContaResponse {
        return ContaResponse(
            TipoConta.CONTA_CORRENTE,
            Instituicao("","60701190"),
            "0001",
            "291900",
            Titular("Valteir", cpf = "63657520325")
        )
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

    @MockBean(ItauClient::class)
    fun itauClient() : ItauClient {
        return Mockito.mock(ItauClient::class.java)
    }

    @MockBean(BancoCentralClient::class)
    fun bcbClient() : BancoCentralClient {
        return Mockito.mock(BancoCentralClient::class.java)
    }

    @Factory
    class Clients {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel) : ConsultaPixServiceGrpc.ConsultaPixServiceBlockingStub {
            return ConsultaPixServiceGrpc.newBlockingStub(channel)
        }
    }
}