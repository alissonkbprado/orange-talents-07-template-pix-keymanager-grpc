package br.com.zup.academy.alissonprado.endpoint.removePix

import br.com.zup.academy.alissonprado.validation.ValidaUUID
import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank


@Introspected
data class RemovePixDto(
    @field:NotBlank
    @field:ValidaUUID(message = "Formato de UUID invalido")
    val idPix: String,

    @field:NotBlank
    @field:ValidaUUID(message = "Formato de UUID invalido")
    val idClienteBanco: String
)