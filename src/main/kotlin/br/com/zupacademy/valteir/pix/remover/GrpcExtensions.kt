package br.com.zupacademy.valteir.pix.remover

import br.com.zupacademy.valteir.RemoveChavePixRequest

fun RemoveChavePixRequest.toModel() : RemoveChavePix {
    return RemoveChavePix(pixId, idTitular)
}