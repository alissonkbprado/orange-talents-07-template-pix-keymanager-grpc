package br.com.zup.academy.alissonprado.httpClient.itau.consultaClienteItau

import br.com.zup.academy.alissonprado.Exception.IdNaoEncontradoItauException
import br.com.zup.academy.alissonprado.model.ChavePix
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory

@Singleton
class ConsultaClienteItauService(
    val consultaClienteItauClient: ConsultaClienteItauClient,
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    fun consulta(chavePix: ChavePix) {
        // Consulta ERP Itau
        val responseConsultaClienteErpItau: HttpResponse<ConsultaClienteItauResponse> =
            consultaClienteItauClient.consultaCliente(chavePix.idClienteBanco)

        // Se retornar Status 404 NOT_FOUND, não encontrou dados no ERP Itau
        if (responseConsultaClienteErpItau.status == HttpStatus.NOT_FOUND) {
            logger.warn(
                "Não encontrados dados do cartão com a instituição financeira. ${
                    chavePix.idClienteBanco.replaceAfter(
                        "-",
                        "***"
                    )
                }"
            )
            throw IdNaoEncontradoItauException()
        }
    }
}