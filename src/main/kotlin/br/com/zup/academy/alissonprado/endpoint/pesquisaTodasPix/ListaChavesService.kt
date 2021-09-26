package br.com.zup.academy.alissonprado.endpoint.pesquisaTodasPix

import br.com.zup.academy.alissonprado.ListaChavesPixResponse
import br.com.zup.academy.alissonprado.TipoChave
import br.com.zup.academy.alissonprado.TipoConta
import br.com.zup.academy.alissonprado.repository.ChavePixRepository
import br.com.zup.academy.alissonprado.validation.ValidaUUID
import com.google.protobuf.Timestamp
import jakarta.inject.Singleton
import java.time.ZoneId
import javax.validation.constraints.NotBlank


@Singleton
class ListaChavesService(
    private val repository: ChavePixRepository
) {

    fun pesquisa(@NotBlank @ValidaUUID idClientBanco: String?): List<ListaChavesPixResponse.ChavePix> {
        val chavesPix = repository.findByIdClienteBanco(idClientBanco).map { chavePix ->
            ListaChavesPixResponse.ChavePix.newBuilder()
                .setIdPix(chavePix.idPix)
                .setTipoChave(TipoChave.valueOf(chavePix.tipoChave.name))
                .setChave(chavePix.chave)
                .setTipoConta(TipoConta.valueOf(chavePix.tipoConta.name))
                .setCriadaEm(chavePix.criadaEm.let { LocalDateTime ->
                    val createdAt = LocalDateTime.atZone(ZoneId.of("UTC")).toInstant()
                    Timestamp.newBuilder()
                        .setSeconds(createdAt.epochSecond)
                        .setNanos(createdAt.nano)
                        .build()
                })
                .build()
        }

        return chavesPix
    }

}
