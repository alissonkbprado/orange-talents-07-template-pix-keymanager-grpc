package br.com.zup.academy.alissonprado.endpoint.registraPix

import br.com.zup.academy.alissonprado.Exception.ChaveCadastradaException
import br.com.zup.academy.alissonprado.RegistraPixRequest
import br.com.zup.academy.alissonprado.httpClient.bcb.cadastraChavePixBcb.CadastraChavePixBcbService
import br.com.zup.academy.alissonprado.httpClient.itau.consultaCartaoItau.ConsultaCartaoItauService
import br.com.zup.academy.alissonprado.model.ChavePix
import br.com.zup.academy.alissonprado.model.TipoChave
import br.com.zup.academy.alissonprado.model.TipoConta
import br.com.zup.academy.alissonprado.model.TipoPessoa
import br.com.zup.academy.alissonprado.repository.ChavePixRepository
import io.micronaut.validation.Validated
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory
import java.util.*
import javax.validation.Valid

@Validated
@Singleton
class RegistraPixService(
    val repository: ChavePixRepository,
    val consultaCartaoItauService: ConsultaCartaoItauService,
    val cadastraChavePixBcbService: CadastraChavePixBcbService
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    fun valida(@Valid registraPixDto: RegistraPixDto) {}

    fun registra(request: RegistraPixRequest): String {

        val registraPixDto = RegistraPixDto(
            request.idClienteBanco,
            TipoConta.valueOf(request.tipoConta.toString()),
            TipoChave.valueOf(request.tipoChave.toString()),
            request.chave
        )

        this.valida(registraPixDto)

        // Verifica se a chave já está cadastrada no Banco de Dados
        if (repository.existsByChave(request.chave)) {
            logger.info("Chave Pix register attempted fail (ALREADY_EXISTS): ${request.idClienteBanco.replaceAfter("-","***")} ${request.chave}")
            throw ChaveCadastradaException()
        }

        // Realiza consulta HTTP GET a ERP do Itau
        val consultaContaItauResponse = consultaCartaoItauService.consulta(request)

        val chavePix = ChavePix(
            idClienteBanco = registraPixDto.idClienteBanco,
            tipoChave = registraPixDto.tipoChave,
            tipoConta = registraPixDto.tipoConta,
            tipoPessoa = TipoPessoa.PESSOA_FISICA,
            chave = registraPixDto.chave,
            conta = consultaContaItauResponse.toModel()
        )

        // Realiza requisição HTTP POST e cadastra Chave Pix no Banco Central
        val createPixKeyResponse = cadastraChavePixBcbService.cadastra(chavePix)

        // Se a chave for do tipo aleatoria, adicionamos a chave recebida pelo Banco Central
        if (chavePix.tipoChave == TipoChave.ALEATORIA)
            chavePix.adicionaChaveBancoCentral(createPixKeyResponse.key)

        repository.save(chavePix)

        logger.info("Chave Pix registered: ${chavePix.chave.replaceAfter("-", "***")}")

        return chavePix.idPix
    }


}