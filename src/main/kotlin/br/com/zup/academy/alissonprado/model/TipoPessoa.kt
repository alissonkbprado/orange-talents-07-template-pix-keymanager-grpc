package br.com.zup.academy.alissonprado.model

import br.com.zup.academy.alissonprado.httpClient.bcb.cadastraChavePixBcb.dto.TypeOwner

enum class TipoPessoa {
    PESSOA_FISICA,
    PESSOA_JURIDICA
}

fun TipoPessoa.getTipoPessoaBcb(): TypeOwner {
    when (name) {
        "PESSOA_FISICA" -> return TypeOwner.NATURAL_PERSON
        else -> return TypeOwner.LEGAL_PERSON
    }
}