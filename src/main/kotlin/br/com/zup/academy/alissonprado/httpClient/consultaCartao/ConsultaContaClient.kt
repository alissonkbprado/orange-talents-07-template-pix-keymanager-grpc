package br.com.zup.academy.alissonprado.httpClient.consultaCartao

import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client
import java.util.*

@Client("\${api_erp_itau}")
interface ConsultaContaClient {

    // http://localhost:9091/api/v1/clientes/c56dfef4-7901-44fb-84e2-a2cefb157890/contas?tipo=CONTA_CORRENTE
    @Get(value = "/{clienteId}/contas?tipo={tipoConta}")
    fun consultaConta(
        @PathVariable clienteId: String,
        @QueryValue tipoConta: String
    ): Optional<ConsultaContaResponse>
}