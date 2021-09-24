package br.com.zup.academy.alissonprado.model

import br.com.zup.academy.alissonprado.httpClient.bcb.cadastraChavePixBcb.dto.KeyType

enum class TipoChave {
    CPF,
    CELULAR,
    EMAIL,
    ALEATORIA
}

fun TipoChave.getTipoChaveBcb(): KeyType {
    when (this.name) {
        "CPF" -> return KeyType.CPF
        "CELULAR" -> return KeyType.PHONE
        "EMAIL" -> return KeyType.EMAIL
        else -> return KeyType.RANDOM
    }
}