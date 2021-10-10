package br.com.zup.academy.alissonprado.endpoint.registraPix

import br.com.zup.academy.alissonprado.RegistraPixRequest
import br.com.zup.academy.alissonprado.RegistraPixServiceGrpc
import br.com.zup.academy.alissonprado.TipoChave
import br.com.zup.academy.alissonprado.TipoConta
import br.com.zup.academy.alissonprado.endpoint.removePix.RemovePixServiceTest
import br.com.zup.academy.alissonprado.httpClient.InstituicaoResponse
import br.com.zup.academy.alissonprado.httpClient.bcb.cadastraChavePixBcb.CadastraChavePixBcbClient
import br.com.zup.academy.alissonprado.httpClient.bcb.cadastraChavePixBcb.CadastraChavePixBcbService
import br.com.zup.academy.alissonprado.httpClient.bcb.cadastraChavePixBcb.dto.*
import br.com.zup.academy.alissonprado.httpClient.bcb.removeChavePixBcb.dto.RemovePixKeyRequest
import br.com.zup.academy.alissonprado.httpClient.bcb.removeChavePixBcb.dto.RemovePixKeyResponse
import br.com.zup.academy.alissonprado.httpClient.itau.consultaCartaoItau.ConsultaCartaoItauService
import br.com.zup.academy.alissonprado.httpClient.itau.consultaCartaoItau.ConsultaContaItauClient
import br.com.zup.academy.alissonprado.httpClient.itau.consultaCartaoItau.ConsultaContaItauResponse
import br.com.zup.academy.alissonprado.httpClient.itau.consultaCartaoItau.TitularResponse
import br.com.zup.academy.alissonprado.model.*
import br.com.zup.academy.alissonprado.repository.ChavePixRepository
import io.grpc.ManagedChannel
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatcher
import org.mockito.Mockito
import org.mockito.internal.matchers.InstanceOf
import org.mockito.internal.progress.ThreadSafeMockingProgress
import java.time.LocalDateTime
import java.util.*

@MicronautTest(transactional = false)
internal class RegistraPixServiceTest(
    val registraPixService: RegistraPixService,
    val repository: ChavePixRepository
) {

    companion object {
        val CLIENTE_ID = UUID.randomUUID().toString()
        val CHAVE = "teste@teste.com"
    }

    @field:Inject
    private lateinit var itauClient: ConsultaContaItauClient

    @field:Inject
    private lateinit var bcbClient: CadastraChavePixBcbClient

    private lateinit var registraPixRequest: RegistraPixRequest

    private lateinit var chavePix: ChavePix

    @BeforeEach
    fun setUp() {
        repository.deleteAll()

        chavePix = ChavePix(
            idClienteBanco = CLIENTE_ID,
            tipoConta = br.com.zup.academy.alissonprado.model.TipoConta.CONTA_CORRENTE,
            tipoChave = br.com.zup.academy.alissonprado.model.TipoChave.EMAIL,
            tipoPessoa = TipoPessoa.PESSOA_FISICA,
            chave = CHAVE,
            conta = ContaAssociada(
                instituicaoNome = "Itau",
                instituicaoIspb = "123456",
                nomeDoTitular = "Teste da Silva",
                documentoDoTitular = "00000000000",
                agencia = "1234",
                numeroDaConta = "123456"
            )
        )

        registraPixRequest = RegistraPixRequest.newBuilder()
            .setIdClienteBanco(CLIENTE_ID)
            .setTipoChave(TipoChave.EMAIL)
            .setChave(CHAVE)
            .setTipoConta(TipoConta.CONTA_CORRENTE)
            .build()

    }


    @Test
    fun `deve resgistrar chave`() {
        Mockito.`when`(
            itauClient.consultaConta(
                clienteId = registraPixRequest.idClienteBanco,
                tipoConta = registraPixRequest.tipoConta.toString()
            )
        ).thenReturn(
            HttpResponse.ok(dadosDaContaResponse())
        )

        Mockito.`when`(
            bcbClient.cadastra(dadosCreatePixKeyRequest())
        ).thenReturn(
            HttpResponse.ok(dadosCreatePixKeyResponse())
        )

        val chavePixCriada = registraPixService.registra(registraPixRequest)

        val chavePixCadastrada = repository.findByIdPix(chavePixCriada)

        assertEquals(chavePixCadastrada?.idPix, chavePixCriada)


    }

    @MockBean(ConsultaContaItauClient::class)
    fun consultaContaClienteItauMock(): ConsultaContaItauClient {
        return Mockito.mock(ConsultaContaItauClient::class.java)
    }

    @MockBean(CadastraChavePixBcbClient::class)
    fun cadastraChavePixBcbClientMock(): CadastraChavePixBcbClient {
        return Mockito.mock(CadastraChavePixBcbClient::class.java)
    }

    private fun dadosDaContaResponse(): ConsultaContaItauResponse {
        return ConsultaContaItauResponse(
            tipo = "CONTA_CORRENTE",
            instituicao = InstituicaoResponse(nome = "UNIBANCO ITAU SA", ispb = "6546734"),
            agencia = "1685",
            numero = "0001",
            titular = TitularResponse(id = RegistraPixEndpointTest.CLIENTE_ID, nome = "Teste", cpf = "00011122233")
        )
    }

    private fun dadosCreatePixKeyRequest(): CreatePixKeyRequest {

        return CreatePixKeyRequest(
            keyType = KeyType.EMAIL,
            key = CHAVE,
            bankAccount = BankAccount(
                participant = "123456",
                branch = "1234",
                accountNumber = "123456",
                accountType = AccountType.CACC
            ),
            owner = Owner(
                type = TypeOwner.NATURAL_PERSON,
                name = "Teste da Silva",
                taxIdNumber = "00000000000"
            ))
    }

    private fun dadosCreatePixKeyResponse(): CreatePixKeyResponse {

        return CreatePixKeyResponse(
            keyType = KeyType.EMAIL,
            key = CHAVE,
            bankAccount = BankAccount(
                participant = "123456",
                branch = "1234",
                accountNumber = "123456",
                accountType = AccountType.CACC
            ),
            owner = Owner(
                type = TypeOwner.NATURAL_PERSON,
                name = "Teste da Silva",
                taxIdNumber = "00000000000"
            ),
            createdAt = LocalDateTime.now())
    }
}

