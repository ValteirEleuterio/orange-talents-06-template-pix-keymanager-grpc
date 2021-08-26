package br.com.zupacademy.valteir.pix.consultar

import br.com.zupacademy.valteir.compartilhado.validation.ValidUUID
import br.com.zupacademy.valteir.outros_sistemas.BancoCentralClient
import br.com.zupacademy.valteir.outros_sistemas.ItauClient
import br.com.zupacademy.valteir.pix.ChavePixNaoEncontradaException
import br.com.zupacademy.valteir.pix.ChavePixRepository
import io.micronaut.core.annotation.Introspected
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Introspected
sealed class Filtro {

    abstract fun filtra(repository: ChavePixRepository, itauClient: ItauClient, bcbClient: BancoCentralClient): ChavePixInfo

    @Introspected
    data class PorPixId(
        @field:NotBlank
        @field:ValidUUID
        val idTitular: String,
        @field:NotBlank
        @field:ValidUUID
        val pixId: String,
    ) : Filtro() {

        private fun pixIdAsUuid(): UUID = UUID.fromString(pixId)
        private fun idTitularAsUuid(): UUID = UUID.fromString(idTitular)

        override fun filtra(repository: ChavePixRepository, itauClient: ItauClient, bcbClient: BancoCentralClient): ChavePixInfo {
            val chave = repository.findByIdAndIdTitular(pixIdAsUuid(), idTitularAsUuid())
                .orElseThrow { ChavePixNaoEncontradaException("Chave Pix não encontrada") }

            val contaRespose = try {
                itauClient.consultaContaCliente(chave.idTitular.toString(), chave.conta.toString())
            } catch (e: HttpClientResponseException) {
                throw IllegalStateException("Falha ao buscar dados da conta do cliente no ERP ITAU")
            }.let {
                if(it.status == HttpStatus.NOT_FOUND)
                    throw IllegalStateException("Cliente nao encontrado no ERP ITAU")

                it.body.get()
            }

            return ChavePixInfo.of(chave, contaRespose)
        }
    }

    @Introspected
    data class PorChave(@field:NotBlank @Size(max = 77) val chave: String) : Filtro() {

        override fun filtra(
            repository: ChavePixRepository,
            itauClient: ItauClient,
            bcbClient: BancoCentralClient
        ): ChavePixInfo {
            val possivelChavePix = repository.findByValor(chave)

            if(possivelChavePix.isPresent) {
                val contaResponse = try {
                    itauClient.consultaContaCliente(possivelChavePix.get().idTitular.toString(), possivelChavePix.get().conta.toString())
                } catch (e: HttpClientResponseException) {
                    throw IllegalStateException("Falha ao buscar dados da conta do cliente no ERP ITAU")
                } .let {
                    if(it.status == HttpStatus.NOT_FOUND)
                        throw IllegalStateException("Cliente nao encontrado no ERP ITAU")

                    it.body.get()
                }

                return ChavePixInfo.of(possivelChavePix.get(), contaResponse)
            }

            val responseBCB = bcbClient.buscar(chave)

            return when(responseBCB.status) {
                HttpStatus.OK -> responseBCB.body()!!.toModel()
                else -> throw ChavePixNaoEncontradaException("Chave Pix não encontrada")
            }
        }
    }

    @Introspected
    class Invalido : Filtro() {

        override fun filtra(
            repository: ChavePixRepository,
            itauClient: ItauClient,
            bcbClient: BancoCentralClient
        ): ChavePixInfo {
            throw IllegalArgumentException("Chave Pix inválida ou não informada")
        }
    }

}
