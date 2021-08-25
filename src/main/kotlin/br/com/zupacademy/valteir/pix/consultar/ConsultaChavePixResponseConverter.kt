package br.com.zupacademy.valteir.pix.consultar

import br.com.zupacademy.valteir.ConsultaChavePixResponse
import br.com.zupacademy.valteir.TipoChave
import com.google.protobuf.Timestamp
import java.time.ZoneId

class ConsultaChavePixResponseConverter {

    fun convert(chavePixInfo: ChavePixInfo) : ConsultaChavePixResponse {
        return ConsultaChavePixResponse.newBuilder()
            .setClientId(chavePixInfo.idTitular?.toString() ?: "")
            .setPixId(chavePixInfo.pixId?.toString() ?: "")
            .setChave(ConsultaChavePixResponse.ChavePix.newBuilder()
                .setTipo(TipoChave.valueOf(chavePixInfo.tipo.name))
                .setChave(chavePixInfo.chave)
                .setConta(ConsultaChavePixResponse.ChavePix.Conta.newBuilder()
                    .setTipo(chavePixInfo.conta.tipo)
                    .setInstituicao(chavePixInfo.conta.instituicao)
                    .setNomeTitular(chavePixInfo.conta.nomeTitular)
                    .setCpfTitular(chavePixInfo.conta.cpfTitular)
                    .setAgencia(chavePixInfo.conta.agencia)
                    .setNumeroDaConta(chavePixInfo.conta.numeroConta)
                    .build()
                )
                .setCriadaEm(chavePixInfo.criadaEm.let {
                    val createdAt = it.atZone(ZoneId.of("UTC")).toInstant()
                    Timestamp.newBuilder()
                        .setSeconds(createdAt.epochSecond)
                        .setNanos(createdAt.nano)
                        .build()
                })
            )
            .build()
    }

}
