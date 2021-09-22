package br.com.zup.academy.alissonprado.httpClient.consultaCartaoItau

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client

@Client("\${api_erp_itau}")
interface ConsultaContaItauCliente {

    @Get(value = "/{clienteId}/contas?tipo={tipoConta}")
    fun consultaConta(
        @PathVariable clienteId: String,
        @QueryValue tipoConta: String
    ): HttpResponse<ConsultaContaItauResponse>
}