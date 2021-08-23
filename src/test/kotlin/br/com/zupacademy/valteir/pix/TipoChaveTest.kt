package br.com.zupacademy.valteir.pix

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestTemplate

internal class TipoChaveTest {

    @Nested
    inner class ALEATORIA {

        @Test
        fun `deve ser valido quando a chave aleatoria for nula ou vazia`() {
            with(TipoChave.ALEATORIA) {
                assertTrue(valida(null))
                assertTrue(valida(""))
            }
        }

        @Test
        fun `nao deve ser valido quando a chave aleatoria possuir um valor`() {
            with(TipoChave.ALEATORIA) {
                assertFalse(valida("CHAVE NAO NULA"))
            }
        }
    }

    @Nested
    inner class EMAIL {

        @Test
        fun `deve ser valido quando a chave email for endereco valido`() {
            with(TipoChave.EMAIL) {
                assertTrue(valida("teste@email.com"))
            }
        }

        @Test
        fun `nao deve ser valido quando chave email for em formato invalido`() {
            with(TipoChave.EMAIL) {
                assertFalse(valida("valter.email.com"))
                assertFalse(valida("valteir@email.com."))
            }
        }


        @Test
        fun `nao deve ser valido quando chave email nao for informada`() {
            with(TipoChave.EMAIL) {
                assertFalse(valida(null))
                assertFalse(valida(""))
            }
        }
    }

    @Nested
    inner class CPF {

        @Test
        fun `deve ser valido quando a chave cpf for valida`() {
            with(TipoChave.CPF) {
                assertTrue(valida("11111111111"))
            }
        }

        @Test
        fun `nao deve ser valido quando a chave cpf for um numero invalido`() {
            with(TipoChave.CPF) {
                assertFalse(valida("111111111112"))
            }
        }

        @Test
        fun `nao deve ser valido quando a chave cpf nao for informada`() {
            with(TipoChave.CPF) {
                assertFalse(valida(null))
                assertFalse(valida(""))
            }
        }
    }

    @Nested
    inner class CELULAR {

        @Test
        fun `deve ser valido quando a chave celular for numero valido`() {
            with(TipoChave.CELULAR) {
                assertTrue(valida("+5544998609280"))
            }
        }

        @Test
        fun `nao deve ser valido quando celular for um numero invalido`() {
            with(TipoChave.CELULAR) {
                assertFalse(valida("1198609280"))
                assertFalse(valida("+11a44457896325"))
            }
        }

        @Test
        fun `nao deve ser valido quando numero celular nao for informado`() {
            with(TipoChave.CELULAR) {
                assertFalse(valida(null))
                assertFalse(valida(""))
            }
        }
    }
}