package br.com.zupacademy.valteir.pix.cadastrar

import br.com.zupacademy.valteir.TipoConta
import br.com.zupacademy.valteir.compartilhado.validation.ValidUUID
import br.com.zupacademy.valteir.outros_sistemas.ContaResponse
import br.com.zupacademy.valteir.pix.ChavePix
import br.com.zupacademy.valteir.pix.TipoChave
import io.micronaut.core.annotation.Introspected
import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@ValidPixKey
@Introspected
data class NovaChavePix(

    @field:ValidUUID
    @field:NotBlank
    val idTitular: String?,
    @field:NotNull
    val tipoChave: TipoChave?,
    @field:Size(max = 77)
    val valor: String?,
    @field:NotNull
    val tipoConta: TipoConta?
) {

    fun toModel(dadosConta: ContaResponse) : ChavePix =
        ChavePix(
            idTitular = UUID.fromString(idTitular),
            tipo = tipoChave!!,
            valor = if(tipoChave == TipoChave.ALEATORIA) UUID.randomUUID().toString() else valor!!,
            conta = tipoConta!!,
            instituicao = dadosConta.instituicao.ispb
        )

}
