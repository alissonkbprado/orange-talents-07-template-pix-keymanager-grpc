package br.com.zup.academy.alissonprado.endpoint.pesquisaPix

import br.com.zup.academy.alissonprado.PesquisaChavePixResponse
import br.com.zup.academy.alissonprado.TipoChave
import br.com.zup.academy.alissonprado.TipoConta

import com.google.protobuf.Timestamp
import java.time.ZoneId

class CarregaChavePixResponseConverter {

    fun convert(chaveInfo: ChavePixInfo): PesquisaChavePixResponse {
        return PesquisaChavePixResponse.newBuilder()
            .setIdClienteBanco(chaveInfo.clienteId ?: "") // Protobuf usa "" como default value para String
            .setIdPix(chaveInfo.pixId ?: "") // Protobuf usa "" como default value para String
            .setChave(PesquisaChavePixResponse.ChavePix
                .newBuilder()
                .setTipo(TipoChave.valueOf(chaveInfo.tipo.name))
                .setChave(chaveInfo.chave)
                .setConta(PesquisaChavePixResponse.ChavePix.ContaInfo.newBuilder()
                    .setTipo(TipoConta.valueOf(chaveInfo.tipoDeConta.name))
                    .setInstituicao(chaveInfo.conta.instituicaoNome)
                    .setNomeDoTitular(chaveInfo.conta.nomeDoTitular)
                    .setCpfDoTitular(chaveInfo.conta.documentoDoTitular)
                    .setAgencia(chaveInfo.conta.agencia)
                    .setNumeroDaConta(chaveInfo.conta.numeroDaConta)
                    .build()
                )
                .setCriadaEm(chaveInfo.registradaEm.let {
                    val createdAt = it.atZone(ZoneId.of("UTC")).toInstant()
                    Timestamp.newBuilder()
                        .setSeconds(createdAt.epochSecond)
                        .setNanos(createdAt.nano)
                        .build()
                })
            )
            .build()
    }

}
