package br.com.zupacademy.valteir.pix.consultar

import br.com.zupacademy.valteir.ConsultaChavePixRequest
import br.com.zupacademy.valteir.ConsultaChavePixResponse
import br.com.zupacademy.valteir.ConsultaPixServiceGrpc
import br.com.zupacademy.valteir.compartilhado.grpc.ErrorHandler
import br.com.zupacademy.valteir.outros_sistemas.BancoCentralClient
import br.com.zupacademy.valteir.outros_sistemas.ItauClient
import br.com.zupacademy.valteir.pix.ChavePixRepository
import io.grpc.stub.StreamObserver
import javax.inject.Singleton
import javax.validation.Validator

@Singleton
open class ConsultaChavePixEndpoint(
    private val validator: Validator,
    private val repository: ChavePixRepository,
    private val itauClient: ItauClient,
    private val bcbClient: BancoCentralClient,
) : ConsultaPixServiceGrpc.ConsultaPixServiceImplBase() {


    @ErrorHandler
    override fun consulta(
        request: ConsultaChavePixRequest,
        responseObserver: StreamObserver<ConsultaChavePixResponse>
    ) {

        val filtro = request.toModel(validator)
        val chavePix = filtro.filtra(repository, itauClient, bcbClient)

        responseObserver.onNext(ConsultaChavePixResponseConverter().convert(chavePix))
        responseObserver.onCompleted()
    }
}