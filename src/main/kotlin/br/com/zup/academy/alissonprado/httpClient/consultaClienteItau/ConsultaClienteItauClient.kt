package br.com.zup.academy.alissonprado.httpClient.consultaClienteItau

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client

@Client("\${api_erp_itau}")
interface ConsultaClienteItauClient {

    @Get(value = "/{clienteId}")
    fun consultaCliente(
        @PathVariable clienteId: String
    ): HttpResponse<ConsultaClienteItauResponse>
}