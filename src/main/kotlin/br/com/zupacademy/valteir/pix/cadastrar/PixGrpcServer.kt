package br.com.zupacademy.valteir.pix.cadastrar

import br.com.zupacademy.valteir.PixRequest
import br.com.zupacademy.valteir.PixResponse
import br.com.zupacademy.valteir.PixServiceGrpc
import br.com.zupacademy.valteir.compartilhado.grpc.ErrorHandler
import br.com.zupacademy.valteir.outros_sistemas.ItauClient
import br.com.zupacademy.valteir.pix.ChavePixRepository
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class PixGrpcServer(
    @Inject private val service: NovaChavePixService,
) : PixServiceGrpc.PixServiceImplBase() {

    @ErrorHandler
    override fun cadastrar(request: PixRequest, responseObserver: StreamObserver<PixResponse>) {

        val novaChave = request.toModel()
        val chavePixCriada = service.cadastrar(novaChave)

        responseObserver.onNext(
            PixResponse.newBuilder()
                .setPixId(chavePixCriada.id.toString())
                .build()
        )
        responseObserver.onCompleted()
    }
}