package br.com.zupacademy.valteir.pix

import br.com.zupacademy.valteir.TipoConta
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Entity
class ChavePix(
    @NotBlank
    @field:Column(nullable = false)
    val idTitular: UUID,

    @NotNull
    @Enumerated(EnumType.STRING)
    @field:Column(nullable = false)
    val tipo: TipoChave,

    @NotBlank
    @Size(max = 77)
    @field:Column(nullable = false, unique = true)
    val valor: String,

    @NotNull
    @Enumerated(EnumType.STRING)
    @field:Column(nullable = false)
    val conta: TipoConta
) {

    @Id
    @GeneratedValue
    val id: UUID? = null
}