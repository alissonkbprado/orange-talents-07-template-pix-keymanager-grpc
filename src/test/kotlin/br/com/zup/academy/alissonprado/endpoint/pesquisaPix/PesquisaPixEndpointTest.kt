package br.com.zup.academy.alissonprado.endpoint.pesquisaPix

import br.com.zup.academy.alissonprado.PesquisaChavePixRequest
import br.com.zup.academy.alissonprado.PesquisaChavePixServiceGrpc
import br.com.zup.academy.alissonprado.httpClient.bcb.pesquisaChavePixBcb.*
import br.com.zup.academy.alissonprado.model.*
import br.com.zup.academy.alissonprado.repository.ChavePixRepository
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import java.time.LocalDateTime
import java.util.*

@MicronautTest(transactional = false)
internal class PesquisaPixEndpointTest(
    val grpcClient: PesquisaChavePixServiceGrpc.PesquisaChavePixServiceBlockingStub,
    val repository: ChavePixRepository
) {

    companion object {
        val CLIENTE_ID = UUID.randomUUID().toString()
        val PIX_ID = UUID.randomUUID().toString()
        val CHAVE = "teste@teste.com"
    }

    @field:Inject
    lateinit var bcbClient: PesquisaChavePixBcbClient

    lateinit var chavePix: ChavePix

    @BeforeEach
    fun setUp() {
        repository.deleteAll()

        chavePix = ChavePix(
            idClienteBanco = CLIENTE_ID,
            tipoConta = TipoConta.CONTA_CORRENTE,
            tipoChave = TipoChave.EMAIL,
            tipoPessoa = TipoPessoa.PESSOA_FISICA,
            chave = CHAVE,
            conta = ContaAssociada(
                instituicaoNome = "Itau",
                instituicaoIspb = "60394079",
                nomeDoTitular = "Teste da Silva",
                documentoDoTitular = "00000000000",
                agencia = "0001",
                numeroDaConta = "1234"
            )
        )
    }

    @Test
    fun `deve retornar dados ao pesquisar por chave cadastrada no banco de dados`() {
        // Adiciona registro no banco
        val chavePixCadastrada = repository.save(chavePix)

        val response = grpcClient.pesquisaPix(
            PesquisaChavePixRequest.newBuilder()
                .setChave(chavePixCadastrada.chave)
                .build()
        )

        assertEquals(chavePixCadastrada.chave, response.chave.chave)
        assertEquals(chavePixCadastrada.conta.nomeDoTitular, response.chave.conta.nomeDoTitular)
    }

    @Test
    fun `deve retornar dados ao pesquisar por idPix cadastrado no banco de dados`() {
        // Adiciona registro no banco
        val chavePixCadastrada = repository.save(chavePix)

        val response = grpcClient.pesquisaPix(
            PesquisaChavePixRequest.newBuilder()
                .setPixId(
                    PesquisaChavePixRequest.FiltroPorPixId.newBuilder()
                        .setIdPix(chavePixCadastrada.idPix)
                        .setIdClienteBanco(chavePixCadastrada.idClienteBanco)
                        .build()
                )
                .build()
        )

        assertEquals(chavePixCadastrada.chave, response.chave.chave)
        assertEquals(chavePixCadastrada.conta.nomeDoTitular, response.chave.conta.nomeDoTitular)
    }


    @Test
    fun `deve retornar dados ao pesquisar por chave cadastrada apenas no Banco Central`() {
        Mockito.`when`(
            bcbClient.pesquisaPorChavePix(CHAVE)
        ).thenReturn(
            HttpResponse.ok(dadosPesquisaPixKeyResponse())
        )

        val response = grpcClient.pesquisaPix(
            PesquisaChavePixRequest.newBuilder()
                .setChave(chavePix.chave)
                .build()
        )

        assertEquals("", response.idPix)
        assertEquals("", response.idClienteBanco)
        assertEquals(CHAVE, response.chave.chave)
        assertEquals("Teste da Silva", response.chave.conta.nomeDoTitular)
    }

    @Test
    fun `deve retornar NOT_FOUND ao pesquisar por idPix de outro usuario`() {
        // Adiciona registro no banco
        val chavePixCadastradaA = repository.save(chavePix)

        val chavePixB = ChavePix(
            idClienteBanco = UUID.randomUUID().toString(),
            tipoConta = TipoConta.CONTA_CORRENTE,
            tipoChave = TipoChave.EMAIL,
            tipoPessoa = TipoPessoa.PESSOA_FISICA,
            chave = "email_b@teste.com",
            conta = ContaAssociada(
                instituicaoNome = "Itau",
                instituicaoIspb = "60394079",
                nomeDoTitular = "Teste da Silva",
                documentoDoTitular = "00000000000",
                agencia = "0001",
                numeroDaConta = "1234"
            )
        )

        val chavePixCadastradaB = repository.save(chavePixB)

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.pesquisaPix(
                PesquisaChavePixRequest.newBuilder()
                    .setPixId(
                        PesquisaChavePixRequest.FiltroPorPixId.newBuilder()
                            .setIdPix(chavePixCadastradaB.idPix)
                            .setIdClienteBanco(chavePixCadastradaA.idClienteBanco)
                            .build()
                    )
                    .build()
            )
        }

        with(error){
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave Pix não encontrada", status.description)
        }

    }

    @Test
    fun `deve retornar erro ao pesquisar por idPix nao cadastrado`() {

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.pesquisaPix(
                PesquisaChavePixRequest.newBuilder()
                    .setPixId(
                        PesquisaChavePixRequest.FiltroPorPixId.newBuilder()
                            .setIdPix(PIX_ID)
                            .setIdClienteBanco(CLIENTE_ID)
                            .build()
                    )
                    .build()
            )
        }

        assertEquals(Status.INVALID_ARGUMENT.code, error.status.code)
        assertEquals("findById.id: must not be null", error.status.description)
    }

    @Test
    fun `deve retornar NOT_FOUND ao pesquisar por chave nao cadastrada`() {

        Mockito.`when`(
            bcbClient.pesquisaPorChavePix(CHAVE)
        ).thenReturn(HttpResponse.notFound())

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.pesquisaPix(
                PesquisaChavePixRequest.newBuilder()
                    .setChave(CHAVE)
                    .build()
            )
        }

        assertEquals(Status.NOT_FOUND.code, error.status.code)
        assertEquals("Chave Pix não encontrada", error.status.description)
    }

    @Test
    fun `deve retornar erro ao pesquisar por idPix invalido`() {

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.pesquisaPix(
                PesquisaChavePixRequest.newBuilder()
                    .setPixId(
                        PesquisaChavePixRequest.FiltroPorPixId.newBuilder()
                            .setIdPix("123456")
                            .setIdClienteBanco(CLIENTE_ID)
                            .build()
                    )
                    .build()
            )
        }

        assertEquals(Status.INVALID_ARGUMENT.code, error.status.code)
        assertEquals("findById.id: must not be null", error.status.description)
    }

    @Test
    fun `deve retornar erro ao pesquisar por idClient invalido`() {

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.pesquisaPix(
                PesquisaChavePixRequest.newBuilder()
                    .setPixId(
                        PesquisaChavePixRequest.FiltroPorPixId.newBuilder()
                            .setIdPix(PIX_ID)
                            .setIdClienteBanco("1234")
                            .build()
                    )
                    .build()
            )
        }

        assertEquals(Status.INVALID_ARGUMENT.code, error.status.code)
        assertEquals("findById.id: must not be null", error.status.description)
    }

    @Test
    fun `deve retornar erro ao pesquisar por chave em branco`() {

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.pesquisaPix(
                PesquisaChavePixRequest.newBuilder()
                    .setChave("")
                    .build()
            )
        }

        assertEquals(Status.INVALID_ARGUMENT.code, error.status.code)
        assertEquals("Chave Pix invalida", error.status.description)
    }

    @Test
    fun `deve retornar erro ao pesquisar por chave for maior que 77 caracteres`() {

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.pesquisaPix(
                PesquisaChavePixRequest.newBuilder()
                    .setChave("123456789112345678911234567891123456789112345678911234567891123456789112345678")
                    .build()
            )
        }

        assertEquals(Status.INVALID_ARGUMENT.code, error.status.code)
        assertEquals("Chave Pix invalida", error.status.description)
    }

    @Test
    fun `deve retornar erro ao nao passar nenhum campo para pesquisa`() {

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.pesquisaPix(
                PesquisaChavePixRequest.newBuilder()
                    .build()
            )
        }

        assertEquals(Status.INVALID_ARGUMENT.code, error.status.code)
        assertEquals("Chave Pix inválida ou não informada", error.status.description)
    }

    private fun dadosPesquisaPixKeyResponse(): PixKeyDetailsResponse {
        return PixKeyDetailsResponse(
            keyType = PixKeyType.EMAIL,
            key = CHAVE,
            bankAccount = BankAccount(
                participant = "60394079",
                branch = "1234",
                accountNumber = "123456",
                accountType = BankAccount.AccountType.CACC
            ),
            owner = Owner(
                type = Owner.OwnerType.NATURAL_PERSON,
                name = "Teste da Silva",
                taxIdNumber = "00000000000"
            ),
            createdAt = LocalDateTime.now()
        )
    }

    @MockBean(PesquisaChavePixBcbClient::class)
    fun PesquisaChavePixBcbMock(): PesquisaChavePixBcbClient {
        return Mockito.mock(PesquisaChavePixBcbClient::class.java)
    }
}

@Factory
class ClientsFactory {

    // channel -> Canal com os dados da requisição
    // GrpcServerChannel.NAME -> Para retornar os dados da conexão, ip e porta gerados pelo contexto de testes do Microunaut
    @Singleton
    fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): PesquisaChavePixServiceGrpc.PesquisaChavePixServiceBlockingStub {
        return PesquisaChavePixServiceGrpc.newBlockingStub(channel)
    }
}

