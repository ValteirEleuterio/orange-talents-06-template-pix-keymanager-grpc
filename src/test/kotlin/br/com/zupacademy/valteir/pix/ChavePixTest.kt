package br.com.zupacademy.valteir.pix

import br.com.zupacademy.valteir.TipoConta
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*

internal class ChavePixTest {


    @Test
    fun `deve pertencer ao cliente`() {
        val idTitular = UUID.randomUUID()
        val outroIdTitular = UUID.randomUUID()

        val chavePix = ChavePix(idTitular, TipoChave.EMAIL, "teste@email.com", TipoConta.CONTA_CORRENTE, "60701190")

        assertTrue(chavePix.pertenceAo(idTitular))
        assertFalse(chavePix.pertenceAo(outroIdTitular))
    }
}