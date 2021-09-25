package br.com.zup.academy.alissonprado.endpoint.pesquisaPix

import br.com.zup.academy.alissonprado.model.ChavePix
import br.com.zup.academy.alissonprado.model.ContaAssociada
import br.com.zup.academy.alissonprado.model.TipoChave
import br.com.zup.academy.alissonprado.model.TipoConta
import java.time.LocalDateTime

data class ChavePixInfo(
    val pixId: String? = null,
    val clienteId: String? = null,
    val tipo: TipoChave,
    val chave: String,
    val tipoDeConta: TipoConta,
    val conta: ContaAssociada,
    val registradaEm: LocalDateTime = LocalDateTime.now()
) {

    companion object {
        fun of(chave: ChavePix): ChavePixInfo {
            return ChavePixInfo(
                pixId = chave.idPix,
                clienteId = chave.idClienteBanco,
                tipo = chave.tipoChave,
                chave = chave.chave,
                tipoDeConta = chave.tipoConta,
                conta = chave.conta,
                registradaEm = chave.criadaEm
            )
        }
    }
}