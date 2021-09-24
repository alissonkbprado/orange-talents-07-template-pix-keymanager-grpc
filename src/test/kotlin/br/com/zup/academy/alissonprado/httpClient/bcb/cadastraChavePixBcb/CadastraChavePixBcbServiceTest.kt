package br.com.zup.academy.alissonprado.httpClient.bcb.cadastraChavePixBcb

import br.com.zup.academy.alissonprado.Exception.ChavePixJaRegistradaBCBException
import br.com.zup.academy.alissonprado.endpoint.removePix.RemovePixDto
import br.com.zup.academy.alissonprado.endpoint.removePix.RemovePixServiceTest
import br.com.zup.academy.alissonprado.httpClient.bcb.cadastraChavePixBcb.dto.BankAccount
import br.com.zup.academy.alissonprado.httpClient.bcb.cadastraChavePixBcb.dto.CreatePixKeyRequest
import br.com.zup.academy.alissonprado.httpClient.bcb.cadastraChavePixBcb.dto.CreatePixKeyResponse
import br.com.zup.academy.alissonprado.httpClient.bcb.cadastraChavePixBcb.dto.Owner
import br.com.zup.academy.alissonprado.httpClient.bcb.removeChavePixBcb.RemoveChavePixBcbClient
import br.com.zup.academy.alissonprado.httpClient.bcb.removeChavePixBcb.dto.RemovePixKeyRequest
import br.com.zup.academy.alissonprado.httpClient.bcb.removeChavePixBcb.dto.RemovePixKeyResponse
import br.com.zup.academy.alissonprado.model.*
import io.grpc.Status
import io.micronaut.http.HttpResponse
import io.micronaut.http.client.exceptions.HttpClientException
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import java.time.LocalDateTime
import java.util.*

@MicronautTest(transactional = false)
internal class CadastraChavePixBcbServiceTest(
    val service: CadastraChavePixBcbService
) {

    companion object {
        val CLIENTE_ID = UUID.randomUUID().toString()
        val CHAVE = "teste@teste.com"
    }

    private lateinit var createPixKeyResponse: CreatePixKeyResponse
    private lateinit var createPixKeyRequest: CreatePixKeyRequest
    private lateinit var chavePix: ChavePix

    @Inject
    lateinit var bcbClient: CadastraChavePixBcbClient

    @BeforeEach
    fun setUp() {
        chavePix = ChavePix(
            idClienteBanco = CLIENTE_ID,
            tipoConta = TipoConta.CONTA_CORRENTE,
            tipoChave = TipoChave.EMAIL,
            tipoPessoa = TipoPessoa.PESSOA_FISICA,
            chave = CHAVE,
            conta = ContaAssociada(
                instituicaoNome = "Itau",
                instituicaoIspb = "265874",
                nomeDoTitular = "Teste",
                documentoDoTitular = "00000000000",
                agencia = "0001",
                numeroDaConta = "1234"
            )
        )

        createPixKeyRequest = CreatePixKeyRequest(
            keyType = chavePix.tipoChave.getTipoChaveBcb(),
            key = chavePix.chave,
            bankAccount = BankAccount(
                participant = chavePix.conta.instituicaoIspb,
                branch = chavePix.conta.agencia,
                accountNumber = chavePix.conta.numeroDaConta,
                accountType = chavePix.tipoConta.getTipoContaBcb()
            ),
            owner = Owner(
                type = chavePix.tipoPessoa.getTipoPessoaBcb(),
                name = chavePix.conta.nomeDoTitular,
                taxIdNumber = chavePix.conta.documentoDoTitular
            )
        )

        createPixKeyResponse = CreatePixKeyResponse(
            keyType = chavePix.tipoChave.getTipoChaveBcb(),
            key = chavePix.chave,
            bankAccount = BankAccount(
                participant = chavePix.conta.instituicaoIspb,
                branch = chavePix.conta.agencia,
                accountNumber = chavePix.conta.numeroDaConta,
                accountType = chavePix.tipoConta.getTipoContaBcb()
            ),
            owner = Owner(
                type = chavePix.tipoPessoa.getTipoPessoaBcb(),
                name = chavePix.conta.nomeDoTitular,
                taxIdNumber = chavePix.conta.documentoDoTitular
            ),
            createdAt = LocalDateTime.now()
        )
    }

    @Test
    fun `deve cadastrar chave no Banco Central`() {
        Mockito.`when`(
            bcbClient.cadastra(createPixKeyRequest)
        ).thenReturn(HttpResponse.created(createPixKeyResponse))

        val response = service.cadastra(chavePix)

        assertEquals(chavePix.chave, response.key)
    }

    @Test
    fun `deve cadastrar chave aleatoria no Banco Central quando TipoChave fo ALEATORIA`() {
        chavePix = ChavePix(
            idClienteBanco = CLIENTE_ID,
            tipoConta = TipoConta.CONTA_CORRENTE,
            tipoChave = TipoChave.ALEATORIA,
            tipoPessoa = TipoPessoa.PESSOA_FISICA,
            chave = CHAVE,
            conta = ContaAssociada(
                instituicaoNome = "Itau",
                instituicaoIspb = "265874",
                nomeDoTitular = "Teste",
                documentoDoTitular = "00000000000",
                agencia = "0001",
                numeroDaConta = "1234"
            )
        )

        val chaveAleatoria = UUID.randomUUID().toString()

        createPixKeyRequest = CreatePixKeyRequest(
            keyType = chavePix.tipoChave.getTipoChaveBcb(),
            key = chavePix.chave,
            bankAccount = BankAccount(
                participant = chavePix.conta.instituicaoIspb,
                branch = chavePix.conta.agencia,
                accountNumber = chavePix.conta.numeroDaConta,
                accountType = chavePix.tipoConta.getTipoContaBcb()
            ),
            owner = Owner(
                type = chavePix.tipoPessoa.getTipoPessoaBcb(),
                name = chavePix.conta.nomeDoTitular,
                taxIdNumber = chavePix.conta.documentoDoTitular
            )
        )

        createPixKeyResponse = CreatePixKeyResponse(
            keyType = chavePix.tipoChave.getTipoChaveBcb(),
            key = chaveAleatoria,
            bankAccount = BankAccount(
                participant = chavePix.conta.instituicaoIspb,
                branch = chavePix.conta.agencia,
                accountNumber = chavePix.conta.numeroDaConta,
                accountType = chavePix.tipoConta.getTipoContaBcb()
            ),
            owner = Owner(
                type = chavePix.tipoPessoa.getTipoPessoaBcb(),
                name = chavePix.conta.nomeDoTitular,
                taxIdNumber = chavePix.conta.documentoDoTitular
            ),
            createdAt = LocalDateTime.now()
        )

        Mockito.`when`(
            bcbClient.cadastra(createPixKeyRequest)
        ).thenReturn(HttpResponse.created(createPixKeyResponse))

        val response = service.cadastra(chavePix)

        assertEquals(chaveAleatoria, response.key)
    }

    @Test
    fun `deve retornar erro ao tentar cadastrar chave ja cadastrada no Banco Central`() {
        Mockito.`when`(
            bcbClient.cadastra(createPixKeyRequest)
        ).thenReturn(HttpResponse.unprocessableEntity())

        val error = assertThrows<ChavePixJaRegistradaBCBException> { service.cadastra(chavePix) }

        assertEquals("Chave Pix já registrada no Banco Central", error.message)
    }

    @Test
    fun `deve retornar erro se ocorrer um erro insperado`() {
        Mockito.`when`(
            bcbClient.cadastra(createPixKeyRequest)
        ).thenThrow(HttpClientException("Erro inesperado ao acessar sistema do Banco Central"))

        val error = assertThrows<HttpClientException> { service.cadastra(chavePix) }

        assertEquals("Erro ao acessar o sistema do Banco Central", error.message)
    }

    @Test
    fun `deve retornar erro se o sistema do Banco Central estiver indisponivel ou retornar erro inesperado`() {
        Mockito.`when`(
            bcbClient.cadastra(createPixKeyRequest)
        ).thenThrow(
            HttpClientException("Não foi possível acessar o sistema do Banco Central")
        )

        val error = assertThrows<HttpClientException> { service.cadastra(chavePix) }

        assertEquals("Erro ao acessar o sistema do Banco Central", error.message)
    }

    @MockBean(CadastraChavePixBcbClient::class)
    fun cadastraChaveBcbMock(): CadastraChavePixBcbClient {
        return Mockito.mock(CadastraChavePixBcbClient::class.java)
    }
}