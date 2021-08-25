package br.com.zupacademy.valteir.pix.listar

import br.com.zupacademy.valteir.ListaChavesPixRequest
import br.com.zupacademy.valteir.ListaChavesPixResponse
import br.com.zupacademy.valteir.ListaPixServiceGrpc
import br.com.zupacademy.valteir.TipoChave
import br.com.zupacademy.valteir.pix.ChavePixRepository
import com.google.protobuf.Timestamp
import io.grpc.stub.StreamObserver
import java.time.ZoneId
import java.util.*
import javax.inject.Singleton

@Singleton
class ListarChavesPixEndpoint(
    private val repository: ChavePixRepository,
) : ListaPixServiceGrpc.ListaPixServiceImplBase() {

    override fun lista(request: ListaChavesPixRequest, responseObserver: StreamObserver<ListaChavesPixResponse>) {
        if(request.idTitular.isNullOrBlank())
            throw IllegalArgumentException("idTitular deve ser preenchido")

        val chaves = repository
            .findByIdTitular(UUID.fromString(request.idTitular))
            .map {
                ListaChavesPixResponse.ChavePix.newBuilder()
                    .setChave(it.valor)
                    .setIdTitular(it.idTitular.toString())
                    .setPixId(it.id.toString())
                    .setTipo(TipoChave.valueOf(it.tipo.name))
                    .setTipoConta(it.conta)
                    .setCriadaEm(it.criadaEm.let {
                        val createdAt = it.atZone(ZoneId.of("UTC")).toInstant()
                        Timestamp.newBuilder()
                            .setSeconds(createdAt.epochSecond)
                            .setNanos(createdAt.nano)
                            .build()
                    })
                    .build()
            }

        responseObserver.onNext(ListaChavesPixResponse.newBuilder().addAllChaves(chaves).build())
        responseObserver.onCompleted()
    }

}