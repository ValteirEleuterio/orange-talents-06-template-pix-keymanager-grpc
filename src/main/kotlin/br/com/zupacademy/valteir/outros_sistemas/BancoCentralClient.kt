package br.com.zupacademy.valteir.outros_sistemas

import br.com.zupacademy.valteir.TipoConta
import br.com.zupacademy.valteir.pix.Instituicoes
import br.com.zupacademy.valteir.pix.TipoChave
import br.com.zupacademy.valteir.pix.consultar.ChavePixInfo
import br.com.zupacademy.valteir.pix.consultar.ContaInfo
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType.APPLICATION_XML
import io.micronaut.http.annotation.*
import io.micronaut.http.client.annotation.Client
import java.time.LocalDateTime

@Client("\${bcb-api}")
interface BancoCentralClient {

    @Post("/api/v1/pix/keys")
    @Produces(APPLICATION_XML)
    @Consumes(APPLICATION_XML)
    fun cadastrar(@Body request: CreatePixKeyRequest) : HttpResponse<CreatePixKeyResponse>

    @Get("/api/v1/pix/keys/{key}")
    @Produces(APPLICATION_XML)
    @Consumes(APPLICATION_XML)
    fun buscar(@PathVariable("key") key: String) : HttpResponse<PixKeyDetailsResponse>

    @Delete("/api/v1/pix/keys/{key}")
    @Produces(APPLICATION_XML)
    @Consumes(APPLICATION_XML)
    fun deletar(@PathVariable("key") key: String, @Body request: DeletePixKeyRequest): HttpResponse<*>
}

data class PixKeyDetailsResponse(
    val keyType: PixKeyType,
    val key: String,
    val bankAccount: BankAccount,
    val owner: Owner,
    val createdAt: LocalDateTime
) {


    fun toModel(): ChavePixInfo {
        return ChavePixInfo(
            tipo = keyType.domainType!!,
            chave = key,
            conta = ContaInfo(
                instituicao =  Instituicoes.nome(bankAccount.participant),
                nomeTitular = owner.name,
                cpfTitular = owner.taxIdNumber,
                agencia = bankAccount.branch,
                numeroConta = bankAccount.accountNumber,
                tipo = when (bankAccount.accountType) {
                    BankAccount.AccountType.CACC -> TipoConta.CONTA_CORRENTE
                    BankAccount.AccountType.SVGS -> TipoConta.CONTA_POUPANCA
                }
            )
        )
    }
}

data class Problem (
    val title: String,
    val detail: String
)

data class DeletePixKeyRequest(
    val key: String,
    val participant: String
)


data class BankAccount(
    val participant: String,
    val branch: String,
    val accountNumber: String,
    val accountType: AccountType
) {

    /**
     * https://open-banking.pass-consulting.com/json_ExternalCashAccountType1Code.html
     */
    enum class AccountType() {

        CACC, // Current: Account used to post debits and credits when no specific account has been nominated
        SVGS;

        companion object {
            fun by(domainType: TipoConta): AccountType {
                return when (domainType) {
                    TipoConta.CONTA_CORRENTE -> CACC
                    else -> SVGS
                }
            }
        }
    }

}

enum class PixKeyType(val domainType: TipoChave?) {

    CPF(TipoChave.CPF),
    CNPJ(null),
    PHONE(TipoChave.CELULAR),
    EMAIL(TipoChave.EMAIL),
    RANDOM(TipoChave.ALEATORIA);

    companion object {

        private val mapping = PixKeyType.values().associateBy(PixKeyType::domainType)

        fun by(domainType: TipoChave): PixKeyType {
            return  mapping[domainType] ?: throw IllegalArgumentException("PixKeyType invalid or not found for $domainType")
        }
    }
}