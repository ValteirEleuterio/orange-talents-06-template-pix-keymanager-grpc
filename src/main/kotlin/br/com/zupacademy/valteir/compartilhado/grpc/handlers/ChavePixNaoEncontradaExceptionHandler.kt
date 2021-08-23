package br.com.zupacademy.valteir.compartilhado.grpc.handlers

import br.com.zupacademy.valteir.compartilhado.grpc.ExceptionHandler
import br.com.zupacademy.valteir.compartilhado.grpc.ExceptionHandler.StatusWithDetails
import br.com.zupacademy.valteir.pix.ChavePixNaoEncontradaException
import io.grpc.Status
import javax.inject.Singleton

@Singleton
class ChavePixNaoEncontradaExceptionHandler : ExceptionHandler<ChavePixNaoEncontradaException> {
    override fun handle(e: ChavePixNaoEncontradaException): StatusWithDetails {
        return StatusWithDetails(
            Status.NOT_FOUND
                .withDescription(e.message)
                .withCause(e)
        )
    }

    override fun supports(e: Exception): Boolean = e is ChavePixNaoEncontradaException
}