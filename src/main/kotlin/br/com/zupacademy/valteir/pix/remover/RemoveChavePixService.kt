package br.com.zupacademy.valteir.pix.remover

import br.com.zupacademy.valteir.pix.ChavePixNaoEncontradaException
import br.com.zupacademy.valteir.pix.ChavePixRepository
import io.micronaut.validation.Validated
import java.util.*
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid

@Validated
@Singleton
class RemoveChavePixService(
    private val repository: ChavePixRepository
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

        repository.delete(chavePix)
    }

}