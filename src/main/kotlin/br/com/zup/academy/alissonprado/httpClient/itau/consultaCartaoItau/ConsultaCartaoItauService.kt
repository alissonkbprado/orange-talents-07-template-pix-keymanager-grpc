package br.com.zup.academy.alissonprado.httpClient.itau.consultaCartaoItau

import br.com.zup.academy.alissonprado.Exception.CpfInconsistenteItauException
import br.com.zup.academy.alissonprado.Exception.IdNaoEncontradoItauException
import br.com.zup.academy.alissonprado.RegistraPixRequest
import br.com.zup.academy.alissonprado.TipoChave
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientException
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory

@Singleton
class ConsultaCartaoItauService(
    val consultaContaClient: ConsultaContaItauClient
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    fun consulta(request: RegistraPixRequest): ConsultaContaItauResponse {
        try {
            // Consulta ERP Itau
            val responseConsultaErpItau: HttpResponse<ConsultaContaItauResponse> =
                consultaContaClient.consultaConta(request.idClienteBanco, request.tipoConta.toString())

            // Se retornar Status 404 NOT_FOUND, não encontrou dados no ERP Itau
            if (responseConsultaErpItau.status == HttpStatus.NOT_FOUND) {
                logger.warn(
                    "Não encontrados dados do cartão com a instituição financeira. ${
                        request.idClienteBanco.replaceAfter(
                            "-",
                            "***"
                        )
                    }"
                )
                throw IdNaoEncontradoItauException()
            }

            // Se a chave for o CPF deve ser o mesmo valor presenta no ERP Itau
            if (request.tipoChave == TipoChave.CPF && request.chave != responseConsultaErpItau.body().titular.cpf) {
                logger.warn(
                    "Tentativa de cadastro de chave como CPF diferente do cadastrado no ERP Itau. " +
                            "${request.idClienteBanco.replaceAfter("-", "***")} - " +
                            "${request.chave}"
                )
                throw CpfInconsistenteItauException()
            }

            return responseConsultaErpItau.body()

        } catch (e: HttpClientException) {
            logger.error("Não foi possível acessar a ERP do Itaú. ${e.localizedMessage}")
            throw HttpClientException("Não foi possível consultar os dados do cartão com o Itau.")
        }
    }
}