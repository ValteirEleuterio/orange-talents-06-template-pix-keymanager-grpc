package br.com.zupacademy.valteir.pix.consultar

import br.com.zupacademy.valteir.ConsultaChavePixRequest
import br.com.zupacademy.valteir.ConsultaChavePixRequest.FiltroCase.*
import javax.validation.ConstraintViolationException
import javax.validation.Validator

fun ConsultaChavePixRequest.toModel(validator: Validator): Filtro {
    val filtro = when(filtroCase!!) {
        PIXID -> pixId.run { Filtro.PorPixId(idTitular, pixId) }
        CHAVE -> Filtro.PorChave(chave)
        FILTRO_NOT_SET -> Filtro.Invalido()
    }

    val violations = validator.validate(filtro)
    if(violations.isNotEmpty()) {
        throw ConstraintViolationException(violations)
    }

    return filtro
}