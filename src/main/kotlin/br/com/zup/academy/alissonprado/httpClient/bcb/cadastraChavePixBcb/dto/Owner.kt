package br.com.zup.academy.alissonprado.httpClient.bcb.cadastraChavePixBcb.dto

data class Owner(
    val type: TypeOwner,
    val name: String,
    val taxIdNumber: String // (CPF - Cadastro de Pessoa Física)
)
