package br.com.zup.academy.alissonprado.httpClient.consultaCartaoItau

import io.micronaut.core.annotation.Introspected

@Introspected
data class InstituicaoResponse(
    val nome: String,
    val ispb: String
)