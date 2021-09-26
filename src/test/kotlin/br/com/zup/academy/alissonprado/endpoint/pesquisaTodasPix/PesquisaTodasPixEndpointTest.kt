package br.com.zup.academy.alissonprado.endpoint.pesquisaTodasPix

import br.com.zup.academy.alissonprado.KeymanagerGrpc
import br.com.zup.academy.alissonprado.ListaChavesPixRequest
import br.com.zup.academy.alissonprado.ListaChavesPixServiceGrpc
import br.com.zup.academy.alissonprado.model.*
import br.com.zup.academy.alissonprado.repository.ChavePixRepository
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

@MicronautTest(transactional = false)
internal class PesquisaTodasPixEndpointTest(
    val grpcClient: ListaChavesPixServiceGrpc.ListaChavesPixServiceBlockingStub,
    val repository: ChavePixRepository
) {

    companion object {
        val CLIENTE_ID = UUID.randomUUID().toString()
        val PIX_ID = UUID.randomUUID().toString()
        val CHAVEA = "teste@teste.com"
        val CHAVEB = "00011122233"
    }

    lateinit var chavePixA: ChavePix
    lateinit var chavePixB: ChavePix

    @BeforeEach
    fun setUp() {
        repository.deleteAll()

        chavePixA = ChavePix(
            idClienteBanco = CLIENTE_ID,
            tipoConta = TipoConta.CONTA_CORRENTE,
            tipoChave = TipoChave.EMAIL,
            tipoPessoa = TipoPessoa.PESSOA_FISICA,
            chave = CHAVEA,
            conta = ContaAssociada(
                instituicaoNome = "Itau",
                instituicaoIspb = "60394079",
                nomeDoTitular = "Teste da Silva",
                documentoDoTitular = "00000000000",
                agencia = "0001",
                numeroDaConta = "1234"
            )
        )

        chavePixB = ChavePix(
            idClienteBanco = CLIENTE_ID,
            tipoConta = TipoConta.CONTA_CORRENTE,
            tipoChave = TipoChave.CPF,
            tipoPessoa = TipoPessoa.PESSOA_FISICA,
            chave = CHAVEB,
            conta = ContaAssociada(
                instituicaoNome = "Itau",
                instituicaoIspb = "60394079",
                nomeDoTitular = "Teste da Silva",
                documentoDoTitular = "00000000000",
                agencia = "0001",
                numeroDaConta = "1234"
            )
        )

        repository.save(chavePixA)
        repository.save(chavePixB)
    }

    @Test
    fun `deve listar todas as chaves ao buscar por idClienteBanco`() {
        val response = grpcClient.listaChavesPix(
            ListaChavesPixRequest.newBuilder()
                .setIdClienteBanco(CLIENTE_ID)
                .build()
        )

        with(response) {
            assertEquals(CLIENTE_ID, idClienteBanco)
            assertEquals(2, chavesCount)
            assertEquals(chavePixA.chave, getChaves(0).chave)
            assertEquals(chavePixB.chave, getChaves(1).chave)
        }
    }

    @Test
    fun `deve retornar uma lista vazia ao buscar por idClienteBanco nao existente`() {

        val chaveInexistente = UUID.randomUUID().toString()

        val response = grpcClient.listaChavesPix(
            ListaChavesPixRequest.newBuilder()
                .setIdClienteBanco(chaveInexistente)
                .build()
        )

        with(response) {
            assertEquals(chaveInexistente, idClienteBanco)
            assertEquals(0, chavesCount)
        }
    }

    @Test
    fun `deve retornar erro ao buscar por idClienteBanco em formato invalido`() {

        val chaveInvalida = "123456"

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.listaChavesPix(
                ListaChavesPixRequest.newBuilder()
                    .setIdClienteBanco(chaveInvalida)
                    .build()
            )
        }

        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Invalid UUID string: $chaveInvalida", status.description)
        }
    }

    @Test
    fun `deve retornar erro ao buscar por idClienteBanco vazio`() {

        val chaveInvalida = ""

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.listaChavesPix(
                ListaChavesPixRequest.newBuilder()
                    .setIdClienteBanco(chaveInvalida)
                    .build()
            )
        }

        assertEquals(Status.INVALID_ARGUMENT.code, error.status.code)
    }
}

@Factory
class ClientsFactory {

    // channel -> Canal com os dados da requisição
    // GrpcServerChannel.NAME -> Para retornar os dados da conexão, ip e porta gerados pelo contexto de testes do Microunaut
    @Bean
    fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): ListaChavesPixServiceGrpc.ListaChavesPixServiceBlockingStub {
        return ListaChavesPixServiceGrpc.newBlockingStub(channel)
    }
}