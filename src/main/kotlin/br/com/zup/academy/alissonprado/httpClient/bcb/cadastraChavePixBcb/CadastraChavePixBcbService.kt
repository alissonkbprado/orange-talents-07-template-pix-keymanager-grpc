package br.com.zup.academy.alissonprado.httpClient.bcb.cadastraChavePixBcb

import br.com.zup.academy.alissonprado.Exception.ChavePixJaRegistradaBCBException
import br.com.zup.academy.alissonprado.httpClient.bcb.cadastraChavePixBcb.dto.BankAccount
import br.com.zup.academy.alissonprado.httpClient.bcb.cadastraChavePixBcb.dto.CreatePixKeyRequest
import br.com.zup.academy.alissonprado.httpClient.bcb.cadastraChavePixBcb.dto.CreatePixKeyResponse
import br.com.zup.academy.alissonprado.httpClient.bcb.cadastraChavePixBcb.dto.Owner
import br.com.zup.academy.alissonprado.model.ChavePix
import br.com.zup.academy.alissonprado.model.getTipoChaveBcb
import br.com.zup.academy.alissonprado.model.getTipoContaBcb
import br.com.zup.academy.alissonprado.model.getTipoPessoaBcb
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientException
import io.micronaut.http.client.exceptions.HttpClientResponseException
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory


@Singleton
class CadastraChavePixBcbService(
    private val cadastraChavePixBcbClient: CadastraChavePixBcbClient
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    fun cadastra(chavePix: ChavePix): CreatePixKeyResponse {
        try {
            val createPixKeyRequest = CreatePixKeyRequest(
                keyType = chavePix.tipoChave.getTipoChaveBcb(),
                key = chavePix.chave,
                bankAccount = BankAccount(
                    participant = chavePix.conta.instituicaoIspb,
                    branch = chavePix.conta.agencia,
                    accountNumber = chavePix.conta.numeroDaConta,
                    accountType = chavePix.tipoConta.getTipoContaBcb()
                ),
                owner = Owner(
                    type = chavePix.tipoPessoa.getTipoPessoaBcb(),
                    name = chavePix.conta.nomeDoTitular,
                    taxIdNumber = chavePix.conta.documentoDoTitular
                )
            )

            // Consulta API BCB
            val createPixKeyResponse: HttpResponse<CreatePixKeyResponse> =
                cadastraChavePixBcbClient.cadastra(createPixKeyRequest = createPixKeyRequest)

            // Se retornar Status 422 Unprocessable Entity, chave Pix já registrada no Banco Central
            if (createPixKeyResponse.status == HttpStatus.UNPROCESSABLE_ENTITY) {
                logger.warn("Chave Pix já registrada no Banco Central. ${chavePix.chave}")
                throw ChavePixJaRegistradaBCBException()
            }

//            if (createPixKeyResponse.status == HttpStatus.CREATED) {
                return createPixKeyResponse.body()
//            }
//            else {
//                logger.error("Erro inesperado ao acessar sistema do Banco Central. ${createPixKeyResponse.body()}")
//                throw HttpClientException("Erro inesperado ao acessar sistema do Banco Central")
//            }
        } catch (e: HttpClientException) {
            logger.error("Erro ao acessar o sistema do Banco Central ${e.localizedMessage}")
            throw HttpClientException("Erro ao acessar o sistema do Banco Central")
        }
    }
}