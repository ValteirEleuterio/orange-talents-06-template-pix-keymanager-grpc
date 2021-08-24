package br.com.zupacademy.valteir.outros_sistemas

import br.com.zupacademy.valteir.TipoConta
import br.com.zupacademy.valteir.pix.TipoChave
import br.com.zupacademy.valteir.pix.cadastrar.NovaChavePix

data class CreatePixKeyRequest(
    val keyType: KeyType,
    val key: String,
    val bankAccount: Account,
    val owner: Owner,
) {
    constructor(chavePix: NovaChavePix, dadosConta: ContaResponse) : this(
        KeyType.fromTipoChave(chavePix.tipoChave!!),
        chavePix.valor!!,
        Account(dadosConta.instituicao.ispb, dadosConta.agencia, dadosConta.numero, AccountType.fromTipoConta(dadosConta.tipo)),
        Owner(dadosConta.titular.nome, dadosConta.titular.cpf)
    )
}

data class Account(
    val participant: String,
    val branch: String,
    val accountNumber: String,
    val accountType: AccountType,
)

data class Owner(
    val name: String,
    val taxIdNumber: String,
) {
    val type: OwnerType = OwnerType.NATURAL_PERSON
}

enum class OwnerType {
    NATURAL_PERSON,
    LEGAL_PERSON
}

enum class AccountType {
    CACC,
    SVGS;

    companion object {
        fun fromTipoConta(tipo: TipoConta) =
            when (tipo) {
                TipoConta.CONTA_CORRENTE -> CACC
                else -> SVGS
            }

    }
}

enum class KeyType {
    CPF,
    PHONE,
    EMAIL,
    RANDOM;


    companion object {
        fun fromTipoChave(tipo: TipoChave): KeyType =
            when (tipo) {
                TipoChave.CPF -> CPF
                TipoChave.CELULAR -> PHONE
                TipoChave.EMAIL -> EMAIL
                else -> RANDOM
            }

    }
}