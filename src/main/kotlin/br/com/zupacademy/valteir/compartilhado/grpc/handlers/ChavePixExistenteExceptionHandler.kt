package br.com.zupacademy.valteir.compartilhado.grpc.handlers

import br.com.zupacademy.valteir.compartilhado.grpc.ExceptionHandler
import br.com.zupacademy.valteir.compartilhado.grpc.ExceptionHandler.StatusWithDetails
import br.com.zupacademy.valteir.pix.ChavePixExistenteException
import io.grpc.Status
import javax.inject.Singleton

@Singleton
class ChavePixExistenteExceptionHandler : ExceptionHandler<ChavePixExistenteException> {

    override fun handle(e: ChavePixExistenteException): StatusWithDetails {
        return StatusWithDetails(Status.ALREADY_EXISTS
                                       .withDescription(e.message)
                                       .withCause(e))
    }

    override fun supports(e: Exception): Boolean {
        return e is ChavePixExistenteException
    }
}