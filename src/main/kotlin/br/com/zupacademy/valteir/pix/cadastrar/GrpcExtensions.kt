package br.com.zupacademy.valteir.pix.cadastrar

import br.com.zupacademy.valteir.PixRequest
import br.com.zupacademy.valteir.TipoChave.*
import br.com.zupacademy.valteir.TipoConta
import br.com.zupacademy.valteir.TipoConta.UNKNOWN_TIPO_CONTA
import br.com.zupacademy.valteir.pix.TipoChave

fun PixRequest.toModel() : NovaChavePix {
    return NovaChavePix(
        idTitular = idTitular,
        tipoChave = when(tipo) {
            UNKNOWN_TIPO_CHAVE -> null
            else -> TipoChave.valueOf(tipo.name)
        },
        valor = valorChave,
        tipoConta = when(conta) {
            UNKNOWN_TIPO_CONTA -> null
            else -> TipoConta.valueOf(conta.name)
        }
    )
}