package br.com.zup.academy.alissonprado.endpoint.registraPix

import br.com.zup.academy.alissonprado.RegistraPixRequest
import br.com.zup.academy.alissonprado.RegistraPixServiceGrpc
import br.com.zup.academy.alissonprado.TipoChave
import br.com.zup.academy.alissonprado.TipoConta
import br.com.zup.academy.alissonprado.httpClient.InstituicaoResponse
import br.com.zup.academy.alissonprado.httpClient.bcb.cadastraChavePixBcb.CadastraChavePixBcbClient
import br.com.zup.academy.alissonprado.httpClient.bcb.cadastraChavePixBcb.dto.*
import br.com.zup.academy.alissonprado.httpClient.itau.consultaCartaoItau.ConsultaContaItauClient
import br.com.zup.academy.alissonprado.httpClient.itau.consultaCartaoItau.ConsultaContaItauResponse
import br.com.zup.academy.alissonprado.httpClient.itau.consultaCartaoItau.TitularResponse
import br.com.zup.academy.alissonprado.model.ChavePix
import br.com.zup.academy.alissonprado.model.ContaAssociada
import br.com.zup.academy.alissonprado.model.TipoChave.EMAIL
import br.com.zup.academy.alissonprado.model.TipoConta.CONTA_CORRENTE
import br.com.zup.academy.alissonprado.model.TipoPessoa.PESSOA_FISICA
import br.com.zup.academy.alissonprado.repository.ChavePixRepository
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.http.client.exceptions.HttpClientException
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatcher
import org.mockito.Mockito
import org.mockito.internal.matchers.InstanceOf
import org.mockito.internal.progress.ThreadSafeMockingProgress
import java.time.LocalDateTime
import java.util.*

@MicronautTest(transactional = false)
internal class RegistraPixEndpointTest(
    val grpcClient: RegistraPixServiceGrpc.RegistraPixServiceBlockingStub,
    val repository: ChavePixRepository
) {

    @Inject
    lateinit var itauClient: ConsultaContaItauClient

    @Inject
    lateinit var bcbClient: CadastraChavePixBcbClient

    companion object {
        val CLIENTE_ID = UUID.randomUUID().toString()
        val CHAVE = "teste@teste.com"
    }

    @BeforeEach
    fun setUp() {
        repository.deleteAll()


    }

    @Test
    fun `deve cadastrar chave no banco`() {
        Mockito.`when`(
            itauClient.consultaConta(
                clienteId = CLIENTE_ID,
                tipoConta = "CONTA_CORRENTE"
            )
        ).thenReturn(
            HttpResponse.ok(dadosDaContaResponse())
        )

        Mockito.`when`(
            bcbClient.cadastra(dadosCreatePixKeyRequest())
        ).thenReturn(
            HttpResponse.ok(dadosCreatePixKeyResponse())
        )

        // Realiza a requisição e guarda o retorno
        val response = grpcClient.registraPix(
            RegistraPixRequest.newBuilder()
                .setIdClienteBanco(CLIENTE_ID)
                .setTipoChave(TipoChave.EMAIL)
                .setChave("teste@teste.com")
                .setTipoConta(TipoConta.CONTA_CORRENTE)
                .build()
        )

        with(response) {
            assertNotNull(idPix)
        }

        val chavePix = repository.findByIdPix(response.idPix)

        assertTrue(chavePix != null)
        assertEquals(response.idPix, chavePix?.idPix)
        assertEquals(CLIENTE_ID, chavePix?.idClienteBanco)
        assertEquals(TipoConta.CONTA_CORRENTE.toString(), chavePix?.tipoConta.toString())
        assertEquals(TipoChave.EMAIL.toString(), chavePix?.tipoChave.toString())
        assertEquals("teste@teste.com", chavePix?.chave)
    }

    @Test
    fun `deve cadastrar chave no banco com chave aleatoria e ignorar campo chave`() {
        Mockito.`when`(
            itauClient.consultaConta(
                clienteId = CLIENTE_ID,
                tipoConta = "CONTA_CORRENTE"
            )
        ).thenReturn(
            HttpResponse.ok(dadosDaContaResponse())
        )

        Mockito.`when`(
            bcbClient.cadastra(dadosCreatePixKeyRequestChaveAleatoria())
        ).thenReturn(
            HttpResponse.ok(dadosCreatePixKeyResponseChaveAleatoria())
        )

        // Realiza a requisição e guarda o retorno
        val response = grpcClient.registraPix(
            RegistraPixRequest.newBuilder()
                .setIdClienteBanco(CLIENTE_ID)
                .setTipoConta(TipoConta.CONTA_CORRENTE)
                .setTipoChave(TipoChave.ALEATORIA)
                .setChave("teste@teste.com")
                .build()
        )

        val chavePix = repository.findByIdPix(response.idPix)

        assertTrue(chavePix != null)
        assertEquals(response.idPix, chavePix?.idPix)
        assertEquals(CLIENTE_ID, chavePix?.idClienteBanco)
        assertEquals(TipoConta.CONTA_CORRENTE.toString(), chavePix?.tipoConta.toString())
        assertEquals(TipoChave.ALEATORIA.toString(), chavePix?.tipoChave.toString())
        assertNotEquals("teste@teste.com", chavePix?.chave)
    }

    @Test
    fun `nao deve cadastrar chave ja cadastrada`() {
        val chavePix = ChavePix(
            idClienteBanco = CLIENTE_ID,
            tipoConta = CONTA_CORRENTE,
            tipoChave = EMAIL,
            tipoPessoa = PESSOA_FISICA,
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

        repository.save(chavePix)

//        `when`(itauCliente.consultaConta(clienteId = CLIENTE_ID, tipoConta = "CONTA_CORRENTE"))
//            .thenReturn(HttpResponse.ok(dadosDaContaResponse()))

        // Realiza a requisição e guarda o erro
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.registraPix(
                RegistraPixRequest.newBuilder()
                    .setIdClienteBanco(CLIENTE_ID)
                    .setTipoConta(TipoConta.CONTA_CORRENTE)
                    .setTipoChave(TipoChave.EMAIL)
                    .setChave(CHAVE)
                    .build()
            )
        }

        // Validações
        with(error) {
            assertEquals(Status.ALREADY_EXISTS.code, status.code)
            assertEquals("Valor de chave informado já está registrado", status.description)
        }
    }

    @Test
    fun `nao deve cadastrar se nao estiver cadastrado no ERP Itau`() {
        Mockito.`when`(itauClient.consultaConta(clienteId = CLIENTE_ID, tipoConta = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.notFound())

        // Realiza a requisição e guarda o erro
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.registraPix(
                RegistraPixRequest.newBuilder()
                    .setIdClienteBanco(CLIENTE_ID)
                    .setTipoConta(TipoConta.CONTA_CORRENTE)
                    .setTipoChave(TipoChave.EMAIL)
                    .setChave(CHAVE)
                    .build()
            )
        }

        // Validações
        with(error) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals(
                "Não encontrados dados do cartão com a instituição financeira",
                status.description
            )
        }
    }

    @Test
    fun `nao deve cadastrar se sistema ERP Itau estiver indisponivel`() {
        Mockito.`when`(itauClient.consultaConta(clienteId = CLIENTE_ID, tipoConta = "CONTA_CORRENTE"))
            .thenThrow(HttpClientException("Connect Error: Connection refused:"))

        // Realiza a requisição e guarda o erro
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.registraPix(
                RegistraPixRequest.newBuilder()
                    .setIdClienteBanco(CLIENTE_ID)
                    .setTipoConta(TipoConta.CONTA_CORRENTE)
                    .setTipoChave(TipoChave.EMAIL)
                    .setChave(CHAVE)
                    .build()
            )
        }

        // Validações
        with(error) {
            assertEquals(Status.INTERNAL.code, status.code)
            assertEquals(
                "Não foi possível consultar os dados do cartão com o Itau.",
                status.description
            )
        }
    }

    @Test
    fun `nao deve cadastrar se tipoChave = CPF e CPF for diferente do cadastrado na ERP Itau`() {
        Mockito.`when`(itauClient.consultaConta(clienteId = CLIENTE_ID, tipoConta = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(dadosDaContaResponse()))

        // Realiza a requisição e guarda o erro
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.registraPix(
                RegistraPixRequest.newBuilder()
                    .setIdClienteBanco(CLIENTE_ID)
                    .setTipoConta(TipoConta.CONTA_CORRENTE)
                    .setTipoChave(TipoChave.CPF)
                    .setChave("00000000000")
                    .build()
            )
        }

        // Validações
        with(error) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals(
                "Valor de CPF da chave diferente do que está cadastrado na instituição financeira",
                status.description
            )
        }
    }

    @Test
    fun `nao deve cadastrar com idClienteBanco vazio`() {
        // Realiza a requisição e guarda o erro
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.registraPix(
                RegistraPixRequest.newBuilder()
                    .setIdClienteBanco("")
                    .setTipoConta(TipoConta.CONTA_CORRENTE)
                    .setTipoChave(TipoChave.EMAIL)
                    .setChave(CHAVE)
                    .build()
            )
        }

        // Validações
        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("valida.registraPixDto.idClienteBanco: Formato de UUID invalido", status.description)
        }
    }

    @Test
    fun `nao deve cadastrar com idClienteBanco UUID invalido`() {
        // Padrão valido: "[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$",

        // Realiza a requisição e guarda o erro
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.registraPix(
                RegistraPixRequest.newBuilder()
                    .setIdClienteBanco("5260263c-a3c1-4727-ae32-3bdb2538841")
                    .setTipoConta(TipoConta.CONTA_CORRENTE)
                    .setTipoChave(TipoChave.EMAIL)
                    .setChave(CHAVE)
                    .build()
            )
        }

        // Validações
        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("valida.registraPixDto.idClienteBanco: Formato de UUID invalido", status.description)
        }
    }

    @Test
    fun `nao deve cadastrar com TipoConta nulo`() {
        // Realiza a requisição e guarda o erro
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.registraPix(
                RegistraPixRequest.newBuilder()
                    .setIdClienteBanco(CLIENTE_ID)
                    .setTipoChave(TipoChave.EMAIL)
                    .setChave(CHAVE)
                    .build()
            )
        }

        // Validações
        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals(
                "No enum constant br.com.zup.academy.alissonprado.model.TipoConta.CONTA_DESCONHECIDA",
                status.description
            )
        }
    }

    @Test
    fun `nao deve cadastrar se TipoConta for zero`() {
        // Realiza a requisição e guarda o erro
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.registraPix(
                RegistraPixRequest.newBuilder()
                    .setIdClienteBanco(CLIENTE_ID)
                    .setTipoConta(TipoConta.forNumber(0))
                    .setTipoChave(TipoChave.EMAIL)
                    .setChave(CHAVE)
                    .build()
            )
        }

        // Validações
        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals(
                "No enum constant br.com.zup.academy.alissonprado.model.TipoConta.CONTA_DESCONHECIDA",
                status.description
            )
        }
    }

    @Test
    fun `nao deve cadastrar se TipoChave for nulo`() {
        // Realiza a requisição e guarda o erro
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.registraPix(
                RegistraPixRequest.newBuilder()
                    .setIdClienteBanco(CLIENTE_ID)
                    .setTipoConta(TipoConta.CONTA_CORRENTE)
                    .setChave(CHAVE)
                    .build()
            )
        }

        // Validações
        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals(
                "No enum constant br.com.zup.academy.alissonprado.model.TipoChave.CHAVE_DESCONHECIDA",
                status.description
            )
        }
    }

    @Test
    fun `nao deve cadastrar se TipoChave for zero`() {
        // Adicionar um valor no Banco
        val chave = "teste@teste.com"

        // Realiza a requisição e guarda o erro
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.registraPix(
                RegistraPixRequest.newBuilder()
                    .setIdClienteBanco(CLIENTE_ID)
                    .setTipoConta(TipoConta.CONTA_CORRENTE)
                    .setTipoChave(TipoChave.forNumber(0))
                    .setChave(chave)
                    .build()
            )
        }

        // Validações
        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals(
                "No enum constant br.com.zup.academy.alissonprado.model.TipoChave.CHAVE_DESCONHECIDA",
                status.description
            )
        }
    }

    @Test
    fun `nao deve cadastrar se chave for maior que 77 caracteres`() {
        // Adicionar um valor no Banco
        val chave = "jfg654j6gf4j6gf4j6fg46j4gf64jg6f45j6gf4j64dfj64fg6j4gf64j6fgd4j6fgd45j64fgj647"

        // Realiza a requisição e guarda o erro
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.registraPix(
                RegistraPixRequest.newBuilder()
                    .setIdClienteBanco(CLIENTE_ID)
                    .setTipoConta(TipoConta.CONTA_CORRENTE)
                    .setTipoChave(TipoChave.ALEATORIA)
                    .setChave(chave)
                    .build()
            )
        }

        // Validações
        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("valida.registraPixDto.chave: size must be between 0 and 77", status.description)
        }
    }

    @Test
    fun `nao deve cadastrar se TipoChave = CPF e chave nao for CPF`() {
        // Adicionar um valor no Banco
        val chave = "teste@teste.com"

        // Realiza a requisição e guarda o erro
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.registraPix(
                RegistraPixRequest.newBuilder()
                    .setIdClienteBanco(CLIENTE_ID)
                    .setTipoConta(TipoConta.CONTA_CORRENTE)
                    .setTipoChave(TipoChave.CPF)
                    .setChave(chave)
                    .build()
            )
        }

        // Validações
        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("valida.registraPixDto: Chave com formato inválido.", status.description)
        }
    }

    @Test
    fun `nao deve cadastrar se TipoChave = CELULAR e chave nao for Celular`() {
        // Adicionar um valor no Banco
        val chave = "teste@teste.com"

        // Realiza a requisição e guarda o erro
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.registraPix(
                RegistraPixRequest.newBuilder()
                    .setIdClienteBanco(CLIENTE_ID)
                    .setTipoConta(TipoConta.CONTA_CORRENTE)
                    .setTipoChave(TipoChave.CELULAR)
                    .setChave(chave)
                    .build()
            )
        }

        // Validações
        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("valida.registraPixDto: Chave com formato inválido.", status.description)
        }
    }

    @Test
    fun `nao deve cadastrar se TipoChave = CELULAR e chave for Celular invalido`() {
        // Formato válido: +5585988714077

        // Adicionar um valor no Banco
        val chave = "5585988714077"

        // Realiza a requisição e guarda o erro
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.registraPix(
                RegistraPixRequest.newBuilder()
                    .setIdClienteBanco(CLIENTE_ID)
                    .setTipoConta(TipoConta.CONTA_CORRENTE)
                    .setTipoChave(TipoChave.CELULAR)
                    .setChave(chave)
                    .build()
            )
        }

        // Validações
        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("valida.registraPixDto: Chave com formato inválido.", status.description)
        }
    }

    @Test
    fun `nao deve cadastrar se TipoChave = EMAIL e chave nao for email`() {
        // Formato válido: +5585988714077

        // Adicionar um valor no Banco
        val chave = "+5585988714077"

        // Realiza a requisição e guarda o erro
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.registraPix(
                RegistraPixRequest.newBuilder()
                    .setIdClienteBanco(CLIENTE_ID)
                    .setTipoConta(TipoConta.CONTA_CORRENTE)
                    .setTipoChave(TipoChave.EMAIL)
                    .setChave(chave)
                    .build()
            )
        }

        // Validações
        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("valida.registraPixDto: Chave com formato inválido.", status.description)
        }
    }

    @Test
    fun `nao deve cadastrar se TipoChave = EMAIL e chave for email invalido`() {
        // Formato válido: +5585988714077

        // Adicionar um valor no Banco
        val chave = "teste@"

        // Realiza a requisição e guarda o erro
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.registraPix(
                RegistraPixRequest.newBuilder()
                    .setIdClienteBanco(CLIENTE_ID)
                    .setTipoConta(TipoConta.CONTA_CORRENTE)
                    .setTipoChave(TipoChave.EMAIL)
                    .setChave(chave)
                    .build()
            )
        }

        // Validações
        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("valida.registraPixDto: Chave com formato inválido.", status.description)
        }
    }

    private fun dadosDaContaResponse(): ConsultaContaItauResponse {
        return ConsultaContaItauResponse(
            tipo = "CONTA_CORRENTE",
            instituicao = InstituicaoResponse("UNIBANCO ITAU SA", "6546734"),
            agencia = "1685",
            numero = "0001",
            titular = TitularResponse(id = CLIENTE_ID, nome = "Teste", cpf = "00011122233")
        )
    }

    private fun dadosCreatePixKeyRequest(): CreatePixKeyRequest {

        reportMatcher(InstanceOf(CreatePixKeyRequest::class.java, "<any createPixKeyRequest>"))

        return CreatePixKeyRequest(
            keyType = KeyType.EMAIL,
            key = "teste@teste.com",
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
            key = "teste@teste.com",
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

    private fun dadosCreatePixKeyRequestChaveAleatoria(): CreatePixKeyRequest {

        reportMatcher(InstanceOf(CreatePixKeyRequest::class.java, "<any createPixKeyRequest>"))

        return CreatePixKeyRequest(
            keyType = KeyType.RANDOM,
            key = "teste@teste.com",
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

    private fun dadosCreatePixKeyResponseChaveAleatoria(): CreatePixKeyResponse {
        return CreatePixKeyResponse(
            keyType = KeyType.RANDOM,
            key = UUID.randomUUID().toString(),
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


    // cópia de como o Mockito cria os métodos .any() de outros tipos
    private fun reportMatcher(matcher: ArgumentMatcher<*>) {
        ThreadSafeMockingProgress.mockingProgress().argumentMatcherStorage.reportMatcher(matcher)
    }


    @MockBean(ConsultaContaItauClient::class)
    fun consultaContaClienteItauMock(): ConsultaContaItauClient {
        return Mockito.mock(ConsultaContaItauClient::class.java)
    }

    @MockBean(CadastraChavePixBcbClient::class)
    fun cadastraChaveBcbMock(): CadastraChavePixBcbClient {
        return Mockito.mock(CadastraChavePixBcbClient::class.java)
    }

}

@Factory
class ClientsFactory {

    // channel -> Canal com os dados da requisição
    // GrpcServerChannel.NAME -> Para retornar os dados da conexão, ip e porta gerados pelo contexto de testes do Microunaut
    @Singleton
    fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): RegistraPixServiceGrpc.RegistraPixServiceBlockingStub {
        return RegistraPixServiceGrpc.newBlockingStub(channel)
    }
}