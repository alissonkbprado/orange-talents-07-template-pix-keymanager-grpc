package br.com.zup.academy.alissonprado.endpoint

import br.com.zup.academy.alissonprado.Exception.ChaveCadastradaException
import br.com.zup.academy.alissonprado.RegistraPixRequest
import br.com.zup.academy.alissonprado.httpClient.consultaCartao.ConsultaCartaoService
import br.com.zup.academy.alissonprado.model.ChavePix
import br.com.zup.academy.alissonprado.model.TipoChave
import br.com.zup.academy.alissonprado.model.TipoConta
import br.com.zup.academy.alissonprado.repository.ChavePixRepository
import io.micronaut.validation.Validated
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory
import java.util.*
import javax.validation.Valid

@Validated
@Singleton
class ResgistraPixService(
    val repository: ChavePixRepository,
    val consultaCartaoService: ConsultaCartaoService
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    fun valida(@Valid registraPixDto: RegistraPixDto): ChavePix {
        return registraPixDto.toModel()
    }

    fun registra(request: RegistraPixRequest): String {

        val registraPixDto = RegistraPixDto(
            request.idClienteBanco,
            TipoConta.valueOf(request.tipoConta.toString()),
            TipoChave.valueOf(request.tipoChave.toString()),
            request.chave
        )

        val chavePix = this.valida(registraPixDto)

        // Verifica se a chave já está cadastrada no Banco de Dados
        if (repository.existsByChave(chavePix.chave)) {
            logger.info("Chave Pix register attempted fail (ALREADY_EXISTS): ${request.idClienteBanco.replaceAfter("-","***")} ${request.chave}")
            throw ChaveCadastradaException()
        }

        // Realiza consulta HTTP GET a ERP do Itau
        consultaCartaoService.Consulta(request)

        repository.save(chavePix)

        logger.info("Chave Pix registered: ${chavePix.idPix.replaceAfter("-", "***")}")

        return chavePix.idPix
    }


}