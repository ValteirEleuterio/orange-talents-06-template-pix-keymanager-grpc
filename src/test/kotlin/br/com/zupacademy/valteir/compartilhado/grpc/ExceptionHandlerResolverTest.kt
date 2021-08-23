package br.com.zupacademy.valteir.compartilhado.grpc

import br.com.zupacademy.valteir.compartilhado.grpc.handlers.DefaultExceptionHandler
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.lang.RuntimeException
import java.util.logging.Handler

internal class ExceptionHandlerResolverTest {
    lateinit var resolver: ExceptionHandlerResolver

    lateinit var illegalArgumentExceptionHandler: ExceptionHandler<IllegalArgumentException>


    @BeforeEach
    fun setup() {
        illegalArgumentExceptionHandler = object : ExceptionHandler<java.lang.IllegalArgumentException> {
            override fun handle(e: java.lang.IllegalArgumentException): ExceptionHandler.StatusWithDetails {
                TODO("Not yet implemented")
            }

            override fun supports(e: Exception) = e is java.lang.IllegalArgumentException

        }

        resolver = ExceptionHandlerResolver(handlers = listOf(illegalArgumentExceptionHandler))
    }


    @Test
    fun `deve retornar o ExceptionHandler especifico para o tipo de excecao`() {

        val handler = resolver.resolve(IllegalArgumentException())

        assertSame(illegalArgumentExceptionHandler, handler)
    }

    @Test
    fun `deve retornar o ExceptionHandler padrao quando nao encontrar handler especifico`() {
        val handler = resolver.resolve(RuntimeException())

        assertTrue(handler is DefaultExceptionHandler)
    }

    @Test
    fun `deve retornar erro caso encontre mais de um ExceptionHandler para a mesma excecao`() {
        val listHandlers = listOf(illegalArgumentExceptionHandler, illegalArgumentExceptionHandler)
        val resolver = ExceptionHandlerResolver(listHandlers)

        val error = assertThrows<IllegalStateException> {
            resolver.resolve(IllegalArgumentException())
        }

        with(error) {
            assertEquals("Too many handlers supporting the same exception '${IllegalArgumentException::class.java.name}': $listHandlers", message)
        }
    }
}