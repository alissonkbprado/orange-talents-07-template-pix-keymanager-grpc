package br.com.zup.academy.alissonprado.model

import br.com.zup.academy.alissonprado.httpClient.bcb.cadastraChavePixBcb.dto.AccountType

enum class TipoConta {
    CONTA_CORRENTE,
    CONTA_POUPANCA
}

fun TipoConta.getTipoContaBcb(): AccountType {
    when (this.name) {
        "CONTA_CORRENTE" -> return AccountType.CACC
        else -> return AccountType.SVGS
    }
}