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

    @Test
    fun `deve ser ALEATORIA`() {
        with(ChavePix(UUID.randomUUID(), TipoChave.ALEATORIA, UUID.randomUUID().toString(), TipoConta.CONTA_CORRENTE, "655879")) {
            assertTrue(ehAleatoria())
        }
    }


    @Test
    fun `nao deve ser ALEATORIA`() {
        with(ChavePix(UUID.randomUUID(), TipoChave.EMAIL, "teste@email.com", TipoConta.CONTA_CORRENTE, "655879")) {
            assertFalse(ehAleatoria())
        }
    }

    @Test
    fun `deve atualizar quando tipo ALEATORIA `() {
        val chavePix = ChavePix(
            UUID.randomUUID(),
            TipoChave.ALEATORIA,
            UUID.randomUUID().toString(),
            TipoConta.CONTA_CORRENTE,
            "666449"
        )

        val novoPixId = UUID.randomUUID().toString()

        val resultado = chavePix.atualiza(novoPixId)

        assertTrue(resultado)
        assertEquals(novoPixId, chavePix.valor)
    }

    @Test
    fun `nao deve atualizar quando tipo for diferente de ALEATORIA`() {
        val valorPix = "teste@email.com"
        val chavePix = ChavePix(
            UUID.randomUUID(),
            TipoChave.EMAIL,
            valorPix,
            TipoConta.CONTA_CORRENTE,
            "465999"
        )

        val resultado = chavePix.atualiza("qualquer coisa")

        assertFalse(resultado)
        assertEquals(valorPix, chavePix.valor)

    }
}