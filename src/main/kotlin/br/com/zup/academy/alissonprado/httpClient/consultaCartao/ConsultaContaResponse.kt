package br.com.zup.academy.alissonprado.httpClient.consultaCartao

import io.micronaut.core.annotation.Introspected

@Introspected
data class ConsultaContaResponse(
    val tipo: String,
    val instituicao: InstituicaoResponse,
    val agencia: String,
    val numero: String,
    val titular: TitularResponse
)