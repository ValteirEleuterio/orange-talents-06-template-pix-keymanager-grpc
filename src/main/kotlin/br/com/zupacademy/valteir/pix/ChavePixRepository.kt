package br.com.zupacademy.valteir.pix

import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface ChavePixRepository : JpaRepository<ChavePix, UUID> {

    fun existsByValor(valor: String) : Boolean

    fun findByValor(valor: String) : Optional<ChavePix>

    fun findByIdAndIdTitular(id: UUID, idTitular: UUID) : Optional<ChavePix>

    fun findByIdTitular(idTitular: UUID) : List<ChavePix>
}