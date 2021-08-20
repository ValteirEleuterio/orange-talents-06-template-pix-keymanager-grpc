package br.com.zupacademy.valteir.outros_sistemas

import com.fasterxml.jackson.annotation.JsonCreator
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType.APPLICATION_XML
import io.micronaut.http.annotation.*
import io.micronaut.http.client.annotation.Client

@Client("\${bcb-api}")
interface BancoCentralClient {

    @Post("/api/v1/pix/keys")
    @Produces(APPLICATION_XML)
    @Consumes(APPLICATION_XML)
    fun cadastrarChavePix(@Body request: CreatePixKeyRequest) : HttpResponse<CreatePixKeyResponse>

    @Get("/api/v1/pix/keys/{key}")
    fun buscaChavePix(@PathVariable("key") key: String) : HttpResponse<PixKeyDetailsResponse>
}

data class PixKeyDetailsResponse(
    val keyType: String,
    val key: String
)

data class Problem (
    val title: String,
    val detail: String
)