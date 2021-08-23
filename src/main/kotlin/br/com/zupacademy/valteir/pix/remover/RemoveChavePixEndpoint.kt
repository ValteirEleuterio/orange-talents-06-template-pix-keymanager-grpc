package br.com.zupacademy.valteir.pix.remover

import br.com.zupacademy.valteir.RemoveChavePixRequest
import br.com.zupacademy.valteir.RemoveChavePixResponse
import br.com.zupacademy.valteir.RemovePixServiceGrpc
import br.com.zupacademy.valteir.compartilhado.grpc.ErrorHandler
import io.grpc.stub.StreamObserver
import javax.inject.Singleton

@Singleton
open class RemoveChavePixEndpoint(
    val service: RemoveChavePixService,
) : RemovePixServiceGrpc.RemovePixServiceImplBase() {


    @ErrorHandler
    override fun remove(request: RemoveChavePixRequest, responseObserver: StreamObserver<RemoveChavePixResponse>) {

        service.remove(request.toModel())

        responseObserver.onNext(RemoveChavePixResponse.newBuilder()
            .setIdTitular(request.idTitular)
            .setPixId(request.pixId)
            .build()
        )
        responseObserver.onCompleted()
    }
}