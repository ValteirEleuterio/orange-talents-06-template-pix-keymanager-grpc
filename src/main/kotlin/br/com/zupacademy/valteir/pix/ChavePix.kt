package br.com.zupacademy.valteir.pix

import br.com.zupacademy.valteir.TipoConta
import java.time.LocalDateTime
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
    var valor: String,

    @NotNull
    @Enumerated(EnumType.STRING)
    @field:Column(nullable = false)
    val conta: TipoConta,

    @field:NotBlank
    @Column(nullable = false)
    val instituicao: String
) {

    @Id
    @GeneratedValue
    val id: UUID? = null

    @Column(nullable = false)
    val criadaEm: LocalDateTime = LocalDateTime.now()

    fun pertenceAo(idTitular : UUID): Boolean = idTitular == this.idTitular

    fun atualiza(novaChave: String) : Boolean {
        if(ehAleatoria()) {
            valor = novaChave
            return true
        }
        return false
    }

    fun ehAleatoria() = tipo == TipoChave.ALEATORIA

}