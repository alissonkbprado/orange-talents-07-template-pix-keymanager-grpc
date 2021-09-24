package br.com.zup.academy.alissonprado.httpClient.bcb.removeChavePixBcb.dto

data class RemovePixKeyResponse(
    val key: String,
    val participant: String,
    val deletedAt: String
)
