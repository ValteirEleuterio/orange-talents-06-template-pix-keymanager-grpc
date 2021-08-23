package br.com.zupacademy.valteir.pix.remover

import br.com.zupacademy.valteir.compartilhado.validation.ValidUUID
import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank

@Introspected
class RemoveChavePix(
    @field:NotBlank
    @field:ValidUUID
    val pixId: String,
    @field:NotBlank
    @field:ValidUUID
    val idTitular: String
)