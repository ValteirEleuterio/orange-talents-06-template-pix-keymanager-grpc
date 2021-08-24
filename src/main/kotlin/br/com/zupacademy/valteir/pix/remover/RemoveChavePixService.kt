package br.com.zupacademy.valteir.pix.remover

import br.com.zupacademy.valteir.outros_sistemas.BancoCentralClient
import br.com.zupacademy.valteir.outros_sistemas.DeletePixKeyRequest
import br.com.zupacademy.valteir.pix.ChavePixNaoEncontradaException
import br.com.zupacademy.valteir.pix.ChavePixRepository
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.validation.Validated
import java.net.http.HttpResponse
import java.util.*
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid

@Validated
@Singleton
class RemoveChavePixService(
    private val repository: ChavePixRepository,
    private val bcbClient: BancoCentralClient
) {

    @Transactional
    fun remove(@Valid removeChavePix: RemoveChavePix) {
        val chavePix = repository
            .findById(UUID.fromString(removeChavePix.pixId))
            .orElseThrow {
                ChavePixNaoEncontradaException("Chave pix: ${removeChavePix.pixId} não encontrada")
            }

        if (!chavePix.pertenceAo(UUID.fromString(removeChavePix.idTitular)))
            throw IllegalStateException("Chave pix não pertence ao titular de id: ${removeChavePix.idTitular}")

        val responseBCB = try {
            bcbClient.deletar(chavePix.valor, DeletePixKeyRequest(chavePix.valor, chavePix.instituicao))
        } catch (e: HttpClientResponseException) {
            throw IllegalStateException("Erro ao remover a chave Pix do Banco Central do Brasil")
        }

        if(responseBCB.status != HttpStatus.OK)
            throw IllegalStateException("Erro ao remover a chave Pix do Banco Central do Brasil")

        repository.delete(chavePix)
    }

}