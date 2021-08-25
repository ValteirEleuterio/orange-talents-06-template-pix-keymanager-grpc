package br.com.zupacademy.valteir.pix.consultar

import br.com.zupacademy.valteir.TipoConta
import br.com.zupacademy.valteir.outros_sistemas.ContaResponse
import br.com.zupacademy.valteir.pix.ChavePix
import br.com.zupacademy.valteir.pix.TipoChave
import java.time.LocalDateTime
import java.util.*

class ChavePixInfo(
    val pixId: UUID? = null,
    val idTitular: UUID? = null,
    val tipo: TipoChave,
    val chave: String,
    val conta: ContaInfo,
    val criadaEm: LocalDateTime = LocalDateTime.now()
) {

    companion object {

        fun of(chavePix: ChavePix, conta: ContaResponse) : ChavePixInfo {
            return ChavePixInfo(
                chavePix.id,
                chavePix.idTitular,
                chavePix.tipo,
                chavePix.valor,
                ContaInfo.of(conta),
                chavePix.criadaEm
            )
        }
    }
}


class ContaInfo(
    val tipo: TipoConta,
    val instituicao: String,
    val nomeTitular: String,
    val cpfTitular: String,
    val agencia: String,
    val numeroConta: String,
) {

    companion object {
        fun of(conta: ContaResponse) : ContaInfo {
            return ContaInfo(
                conta.tipo,
                conta.instituicao.nome,
                conta.titular.nome,
                conta.titular.cpf,
                conta.agencia,
                conta.numero
            )
        }
    }
}
