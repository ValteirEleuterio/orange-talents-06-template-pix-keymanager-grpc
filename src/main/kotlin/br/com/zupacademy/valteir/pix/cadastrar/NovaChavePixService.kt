package br.com.zupacademy.valteir.pix.cadastrar

import br.com.zupacademy.valteir.outros_sistemas.BancoCentralClient
import br.com.zupacademy.valteir.outros_sistemas.CreatePixKeyRequest
import br.com.zupacademy.valteir.outros_sistemas.CreatePixKeyResponse
import br.com.zupacademy.valteir.outros_sistemas.ItauClient
import br.com.zupacademy.valteir.pix.ChavePix
import br.com.zupacademy.valteir.pix.ChavePixExistenteException
import br.com.zupacademy.valteir.pix.ChavePixRepository
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.validation.Validated
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid

@Validated
@Singleton
class NovaChavePixService(
    private val repository: ChavePixRepository,
    private val itauClient: ItauClient,
    private val bcbClient: BancoCentralClient,
) {

    @Transactional
    fun cadastrar(@Valid novaChavePix: NovaChavePix): ChavePix {

        if(repository.existsByValor(novaChavePix.valor!!))
            throw ChavePixExistenteException("Chave pix ${novaChavePix.valor} já está cadastrada")

        val response = itauClient.consultaContaCliente(novaChavePix.idTitular!!, novaChavePix.tipoConta!!.name)
        if(response.status == HttpStatus.NOT_FOUND)
            throw IllegalStateException("Cliente não encontrado no itau")

        val chave = novaChavePix.toModel(response.body.get())
        repository.save(chave)

        val createPixKeyRequest = CreatePixKeyRequest(novaChavePix, response.body.get())
        val responseBCB: HttpResponse<CreatePixKeyResponse> = try {
            bcbClient.cadastrar(createPixKeyRequest)
        } catch (e: HttpClientResponseException) {
            throw IllegalStateException("Falha ao registrar a chave Pix no Banco Central do Brasil (BCB)")
        }

        chave.atualiza(responseBCB.body()!!.key)

        return chave
    }
}

