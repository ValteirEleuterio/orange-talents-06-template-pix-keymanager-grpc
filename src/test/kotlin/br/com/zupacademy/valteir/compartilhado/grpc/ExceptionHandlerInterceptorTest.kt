package br.com.zupacademy.valteir.compartilhado.grpc

import io.grpc.BindableService
import io.grpc.stub.StreamObserver
import io.micronaut.aop.MethodInvocationContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.notNull
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
internal class ExceptionHandlerInterceptorTest {

    @Mock
    lateinit var context: MethodInvocationContext<BindableService, Any?>

    val interceptor = ExceptionHandlerInterceptor(ExceptionHandlerResolver(emptyList()))


    @Test
    fun `deve capturar a excecao lancada pela execucao do metodo, e gerar um erro na resposta gRPC`(@Mock streamObserver : StreamObserver<*>) {
        with(context) {
            `when`(proceed()).thenThrow(RuntimeException("falhou"))
            `when`(parameterValues).thenReturn(arrayOf(null, streamObserver))
        }

        interceptor.intercept(context)

        verify(streamObserver).onError(notNull())
    }


    @Test
    fun `deve retornar a resposta quando nao gerar excecao`() {
        val esperado = "qualquer coisa"
        `when`(context.proceed()).thenReturn(esperado)

        val retorno = interceptor.intercept(context)

        assertEquals(esperado, retorno)
    }
}