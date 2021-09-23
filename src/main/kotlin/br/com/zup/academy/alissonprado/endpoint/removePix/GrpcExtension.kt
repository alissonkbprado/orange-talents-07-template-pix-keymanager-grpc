package br.com.zup.academy.alissonprado.endpoint.removePix

import br.com.zup.academy.alissonprado.RemovePixRequest

fun RemovePixRequest.toDto(): RemovePixDto {
    return RemovePixDto(
        idPix = idPix,
        idClienteBanco = idClienteBanco
    )
}