package br.com.zup.academy.alissonprado.httpClient.consultaClienteItau

import br.com.zup.academy.alissonprado.httpClient.InstituicaoResponse
import io.micronaut.core.annotation.Introspected

@Introspected
data class ConsultaClienteItauResponse(
    val id: String,
    val nome: String,
    val cpf: String,
    val instituicao: InstituicaoResponse
)
