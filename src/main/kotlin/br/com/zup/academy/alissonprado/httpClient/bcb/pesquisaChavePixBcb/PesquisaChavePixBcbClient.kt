package br.com.zup.academy.alissonprado.httpClient.bcb.pesquisaChavePixBcb

import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.client.annotation.Client

@Client("\${api_BCB}")
interface PesquisaChavePixBcbClient {

    @Get(
        "/api/v1/pix/keys/{key}",
        consumes = [MediaType.APPLICATION_XML]
    )
    fun pesquisaPorChavePix(@PathVariable key: String): HttpResponse<PixKeyDetailsResponse>
}