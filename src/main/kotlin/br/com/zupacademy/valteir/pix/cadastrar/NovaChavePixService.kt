package br.com.zupacademy.valteir.pix.cadastrar

import br.com.zupacademy.valteir.outros_sistemas.ItauClient
import br.com.zupacademy.valteir.pix.ChavePix
import br.com.zupacademy.valteir.pix.ChavePixExistenteException
import br.com.zupacademy.valteir.pix.ChavePixRepository
import io.micronaut.http.HttpStatus
import io.micronaut.validation.Validated
import java.lang.IllegalStateException
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid

@Validated
@Singleton
class NovaChavePixService(
    private val repository: ChavePixRepository,
    private val itauClient: ItauClient,
) {

    @Transactional
    fun cadastrar(@Valid novaChavePix: NovaChavePix): ChavePix {

        if(repository.existsByValor(novaChavePix.valor!!))
            throw ChavePixExistenteException("Chave pix ${novaChavePix.valor} já está cadastrada")

        val response = itauClient.consultaContaCliente(novaChavePix.idTitular!!, novaChavePix.tipoConta!!.name)
        if(response.status == HttpStatus.NOT_FOUND)
            throw IllegalStateException("Cliente não encontrado no itau")

        val chave = novaChavePix.toModel()
        repository.save(chave)

        return chave
    }
}
