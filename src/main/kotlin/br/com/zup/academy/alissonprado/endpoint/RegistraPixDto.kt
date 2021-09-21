package br.com.zup.academy.alissonprado.endpoint

import br.com.zup.academy.alissonprado.annotation.ValidPix
import br.com.zup.academy.alissonprado.model.ChavePix
import br.com.zup.academy.alissonprado.model.TipoChave
import br.com.zup.academy.alissonprado.model.TipoConta
import br.com.zup.academy.alissonprado.validation.ValidUUID
import io.micronaut.core.annotation.Introspected
import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@ValidPix
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
) {
//    fun toModel(): ChavePix{
//
//        var chavePix = chave
//        if (tipoChave == TipoChave.ALEATORIA)
//            chavePix = UUID.randomUUID().toString()
//
//        return ChavePix(
//            idClienteBanco = this.idClienteBanco,
//            tipoChave = this.tipoChave,
//            tipoConta = this.tipoConta,
//            chave = chavePix
//        )
//    }
}
