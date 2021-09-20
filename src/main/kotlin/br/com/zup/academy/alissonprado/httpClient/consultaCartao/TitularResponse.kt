package br.com.zup.academy.alissonprado.httpClient.consultaCartao

import io.micronaut.core.annotation.Introspected

@Introspected
data class TitularResponse(
    val id: String,
    val nome: String,
    val cpf: String
)
