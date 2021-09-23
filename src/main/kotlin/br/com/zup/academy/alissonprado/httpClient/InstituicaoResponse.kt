package br.com.zup.academy.alissonprado.httpClient

import io.micronaut.core.annotation.Introspected

@Introspected
data class InstituicaoResponse(
    val nome: String,
    val ispb: String
)