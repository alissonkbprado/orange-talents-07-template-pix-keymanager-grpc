package br.com.zup.academy.alissonprado.httpClient.consultaCartaoItau

import br.com.zup.academy.alissonprado.httpClient.InstituicaoResponse
import br.com.zup.academy.alissonprado.model.ContaAssociada
import io.micronaut.core.annotation.Introspected

@Introspected
data class ConsultaContaItauResponse(
    val tipo: String,
    val instituicao: InstituicaoResponse,
    val agencia: String,
    val numero: String,
    val titular: TitularResponse
) {
    fun toModel(): ContaAssociada {

        return ContaAssociada(
            instituicaoNome = this.instituicao.nome,
            instituicaoIspb = this.instituicao.ispb,
            nomeDoTitular = this.titular.nome,
            agencia = this.agencia,
            cpfDoTitular = this.titular.cpf,
            numeroDaConta = this.numero,
        )
    }
}