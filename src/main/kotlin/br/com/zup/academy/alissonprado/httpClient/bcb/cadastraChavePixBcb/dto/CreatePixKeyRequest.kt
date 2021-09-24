package br.com.zup.academy.alissonprado.httpClient.bcb.cadastraChavePixBcb.dto

data class CreatePixKeyRequest(
    val keyType: KeyType,
    val key: String?,
    val bankAccount: BankAccount,
    val owner: Owner
)
