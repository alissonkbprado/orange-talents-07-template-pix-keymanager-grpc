package br.com.zup.academy.alissonprado.endpoint

import br.com.zup.academy.alissonprado.RegistraPixRequest
import br.com.zup.academy.alissonprado.RegistraPixServiceGrpc
import br.com.zup.academy.alissonprado.TipoChave
import br.com.zup.academy.alissonprado.TipoConta
import br.com.zup.academy.alissonprado.httpClient.consultaCartao.ConsultaContaClient
import br.com.zup.academy.alissonprado.model.ChavePix
import br.com.zup.academy.alissonprado.model.TipoChave.EMAIL
import br.com.zup.academy.alissonprado.model.TipoConta.CONTA_CORRENTE
import br.com.zup.academy.alissonprado.repository.ChavePixRepository
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

@MicronautTest(transactional = false)
internal class RegistraPixEndpointTest(
    val grpcClient: RegistraPixServiceGrpc.RegistraPixServiceBlockingStub,
    val repository: ChavePixRepository
) {

    @field:Inject
    lateinit var consultaContaClient: ConsultaContaClient
    val clientId = UUID.randomUUID().toString()

    @BeforeEach
    fun setUp() {
        repository.deleteAll()
    }

    @Test
    fun `deve cadastrar chave no banco`() {
//        val clientId = UUID.randomUUID().toString()
//        val instituicao = InstituicaoResponse(nome = "ITAÚ UNIBANCO S.A.", "60701190")
//        val titular = TitularResponse(clientId, "Teste", "00011122233")
//        val consultaContaResponse = ConsultaContaResponse(
//            tipo = "CONTA_CORRENTE",
//            instituicao = instituicao,
//            agencia = "0001",
//            numero = "1234",
//            titular = titular
//        )

//        Mockito.`when`(
//            consultaContaClient.consultaConta(
//                clientId,
//                TipoConta.CONTA_CORRENTE.toString()
//            )
//        ).thenReturn(HttpResponse.ok(consultaContaResponse))

        // Realiza a requisição e guarda o retorno
        val response = grpcClient.registraPix(
            RegistraPixRequest.newBuilder()
                .setIdClienteBanco("5260263c-a3c1-4727-ae32-3bdb2538841b")
                .setTipoConta(TipoConta.CONTA_CORRENTE)
                .setTipoChave(TipoChave.EMAIL)
                .setChave("teste@teste.com")
                .build()
        )

        val optionalChavePix = repository.findByIdPix(response.idPix)

        assertTrue(optionalChavePix.isPresent)
        assertEquals(response.idPix, optionalChavePix.get().idPix)
        assertEquals("5260263c-a3c1-4727-ae32-3bdb2538841b", optionalChavePix.get().idClienteBanco)
        assertEquals(TipoConta.CONTA_CORRENTE.toString(), optionalChavePix.get().tipoConta.toString())
        assertEquals(TipoChave.EMAIL.toString(), optionalChavePix.get().tipoChave.toString())
        assertEquals("teste@teste.com", optionalChavePix.get().chave)
    }

    @Test
    fun `deve cadastrar chave no banco com chave aleatoria e ignorar campo chave`() {

        // Realiza a requisição e guarda o retorno
        val response = grpcClient.registraPix(
            RegistraPixRequest.newBuilder()
                .setIdClienteBanco("5260263c-a3c1-4727-ae32-3bdb2538841b")
                .setTipoConta(TipoConta.CONTA_CORRENTE)
                .setTipoChave(TipoChave.ALEATORIA)
                .setChave("teste@teste.com")
                .build()
        )

        val optionalChavePix = repository.findByIdPix(response.idPix)

        assertTrue(optionalChavePix.isPresent)
        assertEquals(response.idPix, optionalChavePix.get().idPix)
        assertEquals("5260263c-a3c1-4727-ae32-3bdb2538841b", optionalChavePix.get().idClienteBanco)
        assertEquals(TipoConta.CONTA_CORRENTE.toString(), optionalChavePix.get().tipoConta.toString())
        assertEquals(TipoChave.ALEATORIA.toString(), optionalChavePix.get().tipoChave.toString())
        assertNotEquals("teste@teste.com", optionalChavePix.get().chave)
    }

    @Test
    fun `nao deve cadastrar chave ja cadastrada`() {
        // Adicionar um valor no Banco
        val clientId = UUID.randomUUID().toString()
        val chave = "teste@teste.com"

        val chavePix = ChavePix(
            idClienteBanco = clientId,
            tipoConta = CONTA_CORRENTE,
            tipoChave = EMAIL,
            chave = chave
        )

        repository.save(chavePix)

        // Realiza a requisição e guarda o erro
        val error = org.junit.jupiter.api.assertThrows<StatusRuntimeException> {
            grpcClient.registraPix(
                RegistraPixRequest.newBuilder()
                    .setIdClienteBanco(clientId)
                    .setTipoConta(TipoConta.CONTA_CORRENTE)
                    .setTipoChave(TipoChave.EMAIL)
                    .setChave(chave)
                    .build()
            )
        }

        // Validações
        with(error) {
            assertEquals(Status.ALREADY_EXISTS.code, status.code)
            assertEquals("ALREADY_EXISTS: Valor de chave informado já está registrado.", this.message)
        }
    }

    @Test
    fun `nao deve cadastrar se tipoChave = CPF e CPF for diferente do cadastrado na ERP Itau`() {
        // Realiza a requisição e guarda o erro
        val error = org.junit.jupiter.api.assertThrows<StatusRuntimeException> {
            grpcClient.registraPix(
                RegistraPixRequest.newBuilder()
                    .setIdClienteBanco("5260263c-a3c1-4727-ae32-3bdb2538841b")
                    .setTipoConta(TipoConta.CONTA_CORRENTE)
                    .setTipoChave(TipoChave.CPF)
                    .setChave("00000000000")
                    .build()
            )
        }

        // Validações
        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("INVALID_ARGUMENT: INVALID_ARGUMENT: Valor de CPF da chave diferente do que está cadastrado na instituição financeira.", this.message)
        }
    }

    @Test
    fun `nao deve cadastrar se nao estiver cadastrado no ERP Itau`() {
        // Adicionar um valor no Banco
        val chave = "teste@teste.com"

        // Realiza a requisição e guarda o erro
        val error = org.junit.jupiter.api.assertThrows<StatusRuntimeException> {
            grpcClient.registraPix(
                RegistraPixRequest.newBuilder()
                    .setIdClienteBanco(clientId)
                    .setTipoConta(TipoConta.CONTA_CORRENTE)
                    .setTipoChave(TipoChave.EMAIL)
                    .setChave(chave)
                    .build()
            )
        }

        // Validações
        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals(
                "INVALID_ARGUMENT: INVALID_ARGUMENT: Não encontrados dados do cartão com a instituição financeira.",
                this.message
            )
        }
    }

    @Test
    fun `nao deve cadastrar com idClienteBanco vazio`() {
        // Adicionar um valor no Banco
        val clientId = ""
        val chave = "teste@teste.com"

        // Realiza a requisição e guarda o erro
        val error = org.junit.jupiter.api.assertThrows<StatusRuntimeException> {
            grpcClient.registraPix(
                RegistraPixRequest.newBuilder()
                    .setIdClienteBanco(clientId)
                    .setTipoConta(TipoConta.CONTA_CORRENTE)
                    .setTipoChave(TipoChave.EMAIL)
                    .setChave(chave)
                    .build()
            )
        }

        // Validações
        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("INVALID_ARGUMENT: valida.registraPixDto.idClienteBanco: must not be blank", this.message)
        }
    }

    @Test
    fun `nao deve cadastrar com idClienteBanco invalido`() {
        // Adicionar um valor no Banco
        val clientId = "1234"
        val chave = "teste@teste.com"

        // Realiza a requisição e guarda o erro
        val error = org.junit.jupiter.api.assertThrows<StatusRuntimeException> {
            grpcClient.registraPix(
                RegistraPixRequest.newBuilder()
                    .setIdClienteBanco(clientId)
                    .setTipoConta(TipoConta.CONTA_CORRENTE)
                    .setTipoChave(TipoChave.EMAIL)
                    .setChave(chave)
                    .build()
            )
        }

        // Validações
        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("INVALID_ARGUMENT: INVALID_ARGUMENT: Id passado para consulta incorreto.", this.message)
        }
    }

    @Test
    fun `nao deve cadastrar com TipoConta nulo`() {
        // Adicionar um valor no Banco
        val chave = "teste@teste.com"

        // Realiza a requisição e guarda o erro
        val error = org.junit.jupiter.api.assertThrows<StatusRuntimeException> {
            grpcClient.registraPix(
                RegistraPixRequest.newBuilder()
                    .setIdClienteBanco(clientId)
                    .setTipoChave(TipoChave.EMAIL)
                    .setChave(chave)
                    .build()
            )
        }

        // Validações
        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("INVALID_ARGUMENT: TipoConta ou TipoChave com valor inválido", this.message)
        }
    }

    @Test
    fun `nao deve cadastrar se TipoConta for zero`() {
        // Adicionar um valor no Banco
        val chave = "teste@teste.com"

        // Realiza a requisição e guarda o erro
        val error = org.junit.jupiter.api.assertThrows<StatusRuntimeException> {
            grpcClient.registraPix(
                RegistraPixRequest.newBuilder()
                    .setIdClienteBanco(clientId)
                    .setTipoConta(TipoConta.forNumber(0))
                    .setTipoChave(TipoChave.EMAIL)
                    .setChave(chave)
                    .build()
            )
        }

        // Validações
        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("INVALID_ARGUMENT: TipoConta ou TipoChave com valor inválido", this.message)
        }
    }

    @Test
    fun `nao deve cadastrar se TipoChave for nulo`() {
        // Adicionar um valor no Banco
        val chave = "teste@teste.com"

        // Realiza a requisição e guarda o erro
        val error = org.junit.jupiter.api.assertThrows<StatusRuntimeException> {
            grpcClient.registraPix(
                RegistraPixRequest.newBuilder()
                    .setIdClienteBanco(clientId)
                    .setTipoConta(TipoConta.CONTA_CORRENTE)
                    .setChave(chave)
                    .build()
            )
        }

        // Validações
        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("INVALID_ARGUMENT: TipoConta ou TipoChave com valor inválido", this.message)
        }
    }

    @Test
    fun `nao deve cadastrar se TipoChave for zero`() {
        // Adicionar um valor no Banco
        val chave = "teste@teste.com"

        // Realiza a requisição e guarda o erro
        val error = org.junit.jupiter.api.assertThrows<StatusRuntimeException> {
            grpcClient.registraPix(
                RegistraPixRequest.newBuilder()
                    .setIdClienteBanco(clientId)
                    .setTipoConta(TipoConta.CONTA_CORRENTE)
                    .setTipoChave(TipoChave.forNumber(0))
                    .setChave(chave)
                    .build()
            )
        }

        // Validações
        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("INVALID_ARGUMENT: TipoConta ou TipoChave com valor inválido", this.message)
        }
    }

    @Test
    fun `nao deve cadastrar se chave for maior que 77 caracteres`() {
        // Adicionar um valor no Banco
        val chave = "jfg654j6gf4j6gf4j6fg46j4gf64jg6f45j6gf4j64dfj64fg6j4gf64j6fgd4j6fgd45j64fgj647"

        // Realiza a requisição e guarda o erro
        val error = org.junit.jupiter.api.assertThrows<StatusRuntimeException> {
            grpcClient.registraPix(
                RegistraPixRequest.newBuilder()
                    .setIdClienteBanco(clientId)
                    .setTipoConta(TipoConta.CONTA_CORRENTE)
                    .setTipoChave(TipoChave.ALEATORIA)
                    .setChave(chave)
                    .build()
            )
        }

        // Validações
        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("INVALID_ARGUMENT: valida.registraPixDto.chave: size must be between 0 and 77", this.message)
        }
    }

    @Test
    fun `nao deve cadastrar se TipoChave = CPF se chave nao for CPF`() {
        // Adicionar um valor no Banco
        val chave = "teste@teste.com"

        // Realiza a requisição e guarda o erro
        val error = org.junit.jupiter.api.assertThrows<StatusRuntimeException> {
            grpcClient.registraPix(
                RegistraPixRequest.newBuilder()
                    .setIdClienteBanco(clientId)
                    .setTipoConta(TipoConta.CONTA_CORRENTE)
                    .setTipoChave(TipoChave.CPF)
                    .setChave(chave)
                    .build()
            )
        }

        // Validações
        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("INVALID_ARGUMENT: valida.registraPixDto: Chave com formato inválido.", this.message)
        }
    }

    @Test
    fun `nao deve cadastrar se TipoChave = CELULAR se chave nao for Celular`() {
        // Adicionar um valor no Banco
        val chave = "teste@teste.com"

        // Realiza a requisição e guarda o erro
        val error = org.junit.jupiter.api.assertThrows<StatusRuntimeException> {
            grpcClient.registraPix(
                RegistraPixRequest.newBuilder()
                    .setIdClienteBanco(clientId)
                    .setTipoConta(TipoConta.CONTA_CORRENTE)
                    .setTipoChave(TipoChave.CPF)
                    .setChave(chave)
                    .build()
            )
        }

        // Validações
        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("INVALID_ARGUMENT: valida.registraPixDto: Chave com formato inválido.", this.message)
        }
    }

    @Test
    fun `nao deve cadastrar se TipoChave = CELULAR se chave for Celular invalido`() {
        // Formato válido: +5585988714077

        // Adicionar um valor no Banco
        val chave = "5585988714077"

        // Realiza a requisição e guarda o erro
        val error = org.junit.jupiter.api.assertThrows<StatusRuntimeException> {
            grpcClient.registraPix(
                RegistraPixRequest.newBuilder()
                    .setIdClienteBanco(clientId)
                    .setTipoConta(TipoConta.CONTA_CORRENTE)
                    .setTipoChave(TipoChave.CPF)
                    .setChave(chave)
                    .build()
            )
        }

        // Validações
        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("INVALID_ARGUMENT: valida.registraPixDto: Chave com formato inválido.", this.message)
        }
    }

    @Test
    fun `nao deve cadastrar se TipoChave = EMAIL se chave nao for email`() {
        // Formato válido: +5585988714077

        // Adicionar um valor no Banco
        val chave = "+5585988714077"

        // Realiza a requisição e guarda o erro
        val error = org.junit.jupiter.api.assertThrows<StatusRuntimeException> {
            grpcClient.registraPix(
                RegistraPixRequest.newBuilder()
                    .setIdClienteBanco(clientId)
                    .setTipoConta(TipoConta.CONTA_CORRENTE)
                    .setTipoChave(TipoChave.CPF)
                    .setChave(chave)
                    .build()
            )
        }
    }

    @Test
    fun `nao deve cadastrar se TipoChave = EMAIL se chave for email invalido`() {
        // Formato válido: +5585988714077

        // Adicionar um valor no Banco
        val chave = "teste@"

        // Realiza a requisição e guarda o erro
        val error = org.junit.jupiter.api.assertThrows<StatusRuntimeException> {
            grpcClient.registraPix(
                RegistraPixRequest.newBuilder()
                    .setIdClienteBanco(clientId)
                    .setTipoConta(TipoConta.CONTA_CORRENTE)
                    .setTipoChave(TipoChave.CPF)
                    .setChave(chave)
                    .build()
            )
        }

        // Validações
        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("INVALID_ARGUMENT: valida.registraPixDto: Chave com formato inválido.", this.message)
        }
    }


//    @MockBean(ConsultaContaClient::class)
//    fun consultaContaMock(): ConsultaContaClient {
//        return Mockito.mock(ConsultaContaClient::class.java)
//    }


}