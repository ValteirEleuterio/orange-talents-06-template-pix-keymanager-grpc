package br.com.zupacademy.valteir.outros_sistemas

data class CreatePixKeyRequest(
    val keyType: KeyType,
    val key: String,
    val bankAccount: Account,
    val owner: Owner,
)

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
    SVGS
}

enum class KeyType {
    CPF,
    PHONE,
    EMAIL,
    RANDOM
}