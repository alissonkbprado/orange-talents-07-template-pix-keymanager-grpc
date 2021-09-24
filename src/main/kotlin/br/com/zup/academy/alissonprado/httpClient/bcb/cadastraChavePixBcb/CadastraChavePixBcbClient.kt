package br.com.zup.academy.alissonprado.httpClient.bcb.cadastraChavePixBcb

import br.com.zup.academy.alissonprado.httpClient.bcb.cadastraChavePixBcb.dto.CreatePixKeyRequest
import br.com.zup.academy.alissonprado.httpClient.bcb.cadastraChavePixBcb.dto.CreatePixKeyResponse
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.http.client.annotation.Client

@Client("\${api_BCB}")
interface CadastraChavePixBcbClient {

    @Post(
        value = "/api/v1/pix/keys",
        consumes = [MediaType.APPLICATION_XML],
        produces = [MediaType.APPLICATION_XML]
    )
    fun cadastra(@Body createPixKeyRequest: CreatePixKeyRequest): HttpResponse<CreatePixKeyResponse>
}