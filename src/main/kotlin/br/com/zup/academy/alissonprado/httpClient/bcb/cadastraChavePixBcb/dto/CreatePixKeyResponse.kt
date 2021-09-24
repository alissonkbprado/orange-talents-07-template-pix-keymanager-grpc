package br.com.zup.academy.alissonprado.httpClient.bcb.cadastraChavePixBcb.dto

import java.time.LocalDateTime

data class CreatePixKeyResponse(
    val keyType: KeyType,
    val key: String?,
    val bankAccount: BankAccount,
    val owner: Owner,
    val createdAt: LocalDateTime
)
