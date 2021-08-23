package br.com.zupacademy.valteir.pix

enum class TipoChave {
    CPF {
        override fun valida(chave: String?): Boolean {
            if (chave.isNullOrBlank()) {
                return false
            }

            return chave.matches("[0-9]{11}+".toRegex())
        }
    },
    CELULAR {
        override fun valida(chave: String?): Boolean {
            if (chave.isNullOrBlank()) {
                return false
            }

            return chave.matches("^\\+[1-9][0-9]\\d{1,14}\$".toRegex())
        }
    },
    EMAIL {
        override fun valida(chave: String?): Boolean {
            if (chave.isNullOrBlank()) {
                return false
            }

            return chave.matches("^([\\w\\.\\-]+)@([\\w\\-]+)((\\.(\\w){2,3})+)\$".toRegex())
        }
    },
    ALEATORIA {
        override fun valida(chave: String?): Boolean = chave.isNullOrBlank()
    };

    abstract fun valida(chave: String?): Boolean
}