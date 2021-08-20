package br.com.zupacademy.valteir.outros_sistemas

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client


@Client("\${itau-api}")
interface ItauClient {

    @Get("/api/v1/clientes/{clienteId}/contas")
    fun consultaContaCliente(@PathVariable("clienteId") clienteId: String,
                             @QueryValue("tipo") tipo: String) : HttpResponse<ContaResponse>
}