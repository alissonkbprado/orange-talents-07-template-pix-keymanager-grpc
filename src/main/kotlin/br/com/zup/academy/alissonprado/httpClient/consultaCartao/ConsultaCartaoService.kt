package br.com.zup.academy.alissonprado.httpClient.consultaCartao

import br.com.zup.academy.alissonprado.RegistraPixRequest
import br.com.zup.academy.alissonprado.TipoChave
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientException
import io.micronaut.http.client.exceptions.HttpClientResponseException
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory
import java.util.*

@Singleton
class ConsultaCartaoService(
    val consultaContaClient: ConsultaContaClient
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    fun Consulta(request: RegistraPixRequest) {
        try {
            // Consulta ERP Itau
            val responseConsultaErpItau: Optional<ConsultaContaResponse> =
                consultaContaClient.consultaConta(request.idClienteBanco, request.tipoConta.toString())

            // Se retornar vazio, não encontrou dados no ERP Itau
            if (responseConsultaErpItau.isEmpty) {
                logger.warn(
                    "Não encontrados dados do cartão com a instituição financeira. ${
                        request.idClienteBanco.replaceAfter(
                            "-",
                            "***"
                        )
                    }"
                )
                throw StatusRuntimeException(Status.INVALID_ARGUMENT.withDescription("Não encontrados dados do cartão com a instituição financeira."))
            }

            // Se a chave for o CPF deve ser o mesmo valor presenta no ERP Itau
            if (request.tipoChave == TipoChave.CPF && request.chave != responseConsultaErpItau.get().titular.cpf){
                logger.warn(
                    "Tentativa de cadastro de chave como CPF diferente do cadastrado no ERP Itau. " +
                            "${request.idClienteBanco.replaceAfter("-","***")} - " +
                            "${request.chave}"
                )
                throw StatusRuntimeException(Status.INVALID_ARGUMENT.withDescription("Valor de CPF da chave diferente do que está cadastrado na instituição financeira."))
            }

        } catch (e: HttpClientResponseException) {
                logger.warn("Id passado para consulta incorreto. ${request.idClienteBanco}")
                throw StatusRuntimeException(Status.INVALID_ARGUMENT.withDescription("Id passado para consulta incorreto."))
        } catch (e: HttpClientException) {
            logger.error("Não foi possível acessar a ERP do Itaú. ${e.localizedMessage}")
            throw HttpClientException("Não foi possível consultar os dados do cartão com a instituição financeira.")
        }
    }
}