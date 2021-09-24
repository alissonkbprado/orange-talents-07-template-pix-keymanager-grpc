package br.com.zup.academy.alissonprado.httpClient.itau.consultaClienteItau

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.client.annotation.Client

@Client("\${api_erp_itau}")
interface ConsultaClienteItauClient {

    @Get(value = "/api/v1/clientes/{clienteId}")
    fun consultaCliente(
        @PathVariable clienteId: String
    ): HttpResponse<ConsultaClienteItauResponse>
}