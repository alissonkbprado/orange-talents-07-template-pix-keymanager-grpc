package br.com.zup.academy.alissonprado.httpClient.bcb.pesquisaChavePixBcb.filtro

import br.com.zup.academy.alissonprado.Exception.ChavePixNaoEncontradaException
import br.com.zup.academy.alissonprado.endpoint.pesquisaPix.ChavePixInfo
import br.com.zup.academy.alissonprado.httpClient.bcb.pesquisaChavePixBcb.PesquisaChavePixBcbClient
import br.com.zup.academy.alissonprado.model.ChavePix
import br.com.zup.academy.alissonprado.repository.ChavePixRepository
import br.com.zup.academy.alissonprado.validation.ValidaUUID
import io.micronaut.core.annotation.Introspected
import io.micronaut.http.HttpStatus
import jakarta.inject.Inject
import org.slf4j.LoggerFactory
import javax.validation.ConstraintViolationException
import javax.validation.Validator
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Introspected
sealed class Filtro {

    abstract fun filtra(repository: ChavePixRepository, bcbClient: PesquisaChavePixBcbClient): ChavePixInfo

    @Introspected
    data class PorPixId(
        @field:NotBlank @field:ValidaUUID val clienteId: String,
        @field:NotBlank @field:ValidaUUID val pixId: String,
    ) : Filtro() {

        override fun filtra(repository: ChavePixRepository, bcbClient: PesquisaChavePixBcbClient): ChavePixInfo {

            val chavePix = repository.findByIdPix(pixId)

            return repository.findById(chavePix?.id)
                .filter { it.pertenceAo(clienteId) }
                .map(ChavePixInfo::of)
                .orElseThrow { ChavePixNaoEncontradaException() }
        }
    }

    @Introspected
    data class PorChave(@field:NotBlank @field:Size(max = 77) val chave: String) : Filtro() {

        private val LOGGER = LoggerFactory.getLogger(this::class.java)

        override fun filtra(repository: ChavePixRepository, bcbClient: PesquisaChavePixBcbClient): ChavePixInfo {

            val chavePix = repository.findByChave(chave)

            return repository.findByChave(chave)
                .map(ChavePixInfo::of)
                .orElseGet {
                    LOGGER.info("Consultando chave Pix '$chave' no Banco Central do Brasil (BCB)")

                    val response = bcbClient.pesquisaPorChavePix(chave)
                    when (response.status) {
                        HttpStatus.OK -> response.body()?.toModel()
                        else -> throw ChavePixNaoEncontradaException()
                    }
                }
        }
    }

    @Introspected
    class Invalido() : Filtro() {

        override fun filtra(repository: ChavePixRepository, bcbClient: PesquisaChavePixBcbClient): ChavePixInfo {
            throw IllegalArgumentException("Chave Pix inválida ou não informada")
        }
    }
}
