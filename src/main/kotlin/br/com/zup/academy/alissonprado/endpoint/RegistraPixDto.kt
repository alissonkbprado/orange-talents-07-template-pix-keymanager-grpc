package br.com.zup.academy.alissonprado.endpoint

import br.com.zup.academy.alissonprado.annotation.ValidaPix
import br.com.zup.academy.alissonprado.model.TipoChave
import br.com.zup.academy.alissonprado.model.TipoConta
import br.com.zup.academy.alissonprado.validation.ValidUUID
import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@ValidaPix
@Introspected
data class RegistraPixDto(
    @field:NotNull @field:ValidUUID
    var idClienteBanco: String,

    @field:NotNull
    val tipoConta: TipoConta,

    @field:NotNull
    val tipoChave: TipoChave,

    @field:Size(max = 77)
    val chave: String
)
