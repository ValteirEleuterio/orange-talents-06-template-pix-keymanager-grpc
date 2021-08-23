package br.com.zupacademy.valteir.outros_sistemas

import br.com.zupacademy.valteir.TipoConta

data class ContaResponse(
    val tipo: TipoConta,
    val instituicao: Instituicao,
    val agencia: String,
    val numero: String,
    val titular: Titular,
)

class Instituicao(
    val ispb: String,
)

class Titular(
    val nome: String,
    val cpf: String,
)