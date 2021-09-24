package br.com.zup.academy.alissonprado.httpClient.bcb.removeChavePixBcb

import br.com.zup.academy.alissonprado.Exception.ChavePixNaoEncontradaBCBException
import br.com.zup.academy.alissonprado.httpClient.bcb.removeChavePixBcb.dto.RemovePixKeyRequest
import br.com.zup.academy.alissonprado.httpClient.bcb.removeChavePixBcb.dto.RemovePixKeyResponse
import br.com.zup.academy.alissonprado.model.ChavePix
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientException
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory


@Singleton
class RemoveChavePixBcbService(
    @Inject val removeChavePixBcbClient: RemoveChavePixBcbClient
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    fun remove(chavePix: ChavePix): RemovePixKeyResponse {
        try {
            val removePixKeyRequest = RemovePixKeyRequest(
                key = chavePix.chave,
                participant = chavePix.conta.instituicaoIspb
            )

            // Consulta API BCB
            val removePixKeyResponse: HttpResponse<RemovePixKeyResponse> =
                removeChavePixBcbClient.remove(key = chavePix.chave, removePixKeyRequest = removePixKeyRequest)

            if (removePixKeyResponse == null) {
                logger.warn("Falha ao tentar remover chave Pix no Banco Central. ${chavePix.chave}")
                throw IllegalArgumentException("Falha ao tentar remover chave Pix no Banco Central")
            }

            if (removePixKeyResponse.status == HttpStatus.METHOD_NOT_ALLOWED) {
                logger.warn("Falha ao tentar remover chave Pix no Banco Central. ${chavePix.chave} -> ${removePixKeyResponse.body()}")
                throw IllegalArgumentException("Falha ao tentar remover chave Pix no Banco Central")
            }

            if (removePixKeyResponse.status == HttpStatus.NOT_FOUND) {
                logger.warn("Chave Pix não foi encontrada no Banco Central. ${chavePix.chave}")
                throw ChavePixNaoEncontradaBCBException()
            }


            // Retorna Status 200 OK, a chave foi removida com sucesso no sistema do Banco Central
            logger.info("Chave Pix removida no Banco Central. ${chavePix.chave}")
            return removePixKeyResponse.body()

        } catch (e: HttpClientException) {
            logger.error("Não foi possível acessar os sistema do Banco Central. ${e.localizedMessage}")
            throw HttpClientException("Não foi possível acessar o sistema do Banco Central")
        }
    }
}