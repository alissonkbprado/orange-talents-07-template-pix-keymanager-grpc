package br.com.zup.academy.alissonprado.httpClient.consultaCartao

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client
import java.util.*

@Client("\${api_erp_itau}")
interface ConsultaContaClient {

    @Get(value = "/{clienteId}/contas?tipo={tipoConta}")
    fun consultaConta(
        @PathVariable clienteId: String,
        @QueryValue tipoConta: String
    ): HttpResponse<ConsultaContaResponse>
}