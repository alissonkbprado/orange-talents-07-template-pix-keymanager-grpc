package br.com.zup.academy.alissonprado.endpoint.removePix

import br.com.zup.academy.alissonprado.Exception.ChaveNaoEncontradaException
import br.com.zup.academy.alissonprado.httpClient.bcb.removeChavePixBcb.RemoveChavePixBcbService
import br.com.zup.academy.alissonprado.repository.ChavePixRepository
import io.micronaut.validation.Validated
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory
import javax.transaction.Transactional
import javax.validation.Valid


@Validated
@Singleton
class RemovePixService(
    val repository: ChavePixRepository,
    val removeChavePixBcbService: RemoveChavePixBcbService
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @Transactional
    fun remove(@Valid removePixDto: RemovePixDto): String {

        val chavePix = repository.findByIdPixAndIdClienteBanco(
            idPix = removePixDto.idPix,
            idClienteBanco = removePixDto.idClienteBanco
        ) ?: throw ChaveNaoEncontradaException()

        removeChavePixBcbService.remove(chavePix)

        repository.delete(chavePix)

        logger.info("Chave Pix removida com sucesso. ${chavePix.chave}")

        return "Chave ${chavePix.chave} removida com sucesso"
    }


}
