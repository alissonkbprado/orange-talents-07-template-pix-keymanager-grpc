package br.com.zup.academy.alissonprado.endpoint.removePix

import br.com.zup.academy.alissonprado.Exception.ChaveNaoEncontradaException
import br.com.zup.academy.alissonprado.Exception.ChaveNaoPertenceAoUsuarioException
import br.com.zup.academy.alissonprado.httpClient.consultaCartaoItau.ConsultaClienteItauService
import br.com.zup.academy.alissonprado.httpClient.consultaClienteItau.ConsultaClienteItauClient
import br.com.zup.academy.alissonprado.repository.ChavePixRepository
import io.micronaut.validation.Validated
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory
import javax.transaction.Transactional
import javax.validation.Valid


@Validated
@Singleton
class RemovePixService(
    val repository: ChavePixRepository
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    fun remove(@Valid removePixDto: RemovePixDto): String {

        val chavePix = repository.findByIdPixAndIdClienteBanco(
            idPix = removePixDto.idPix,
            idClienteBanco = removePixDto.idClienteBanco
        ) ?: throw ChaveNaoEncontradaException()

        repository.delete(chavePix)

        return "Chave ${chavePix.chave} removida com sucesso"
    }


}
