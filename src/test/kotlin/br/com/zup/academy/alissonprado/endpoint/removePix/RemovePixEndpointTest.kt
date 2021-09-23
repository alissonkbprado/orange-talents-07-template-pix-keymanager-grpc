package br.com.zup.academy.alissonprado.endpoint.removePix


import br.com.zup.academy.alissonprado.RemovePixRequest
import br.com.zup.academy.alissonprado.RemovePixServiceGrpc
import br.com.zup.academy.alissonprado.model.ChavePix
import br.com.zup.academy.alissonprado.model.ContaAssociada
import br.com.zup.academy.alissonprado.model.TipoChave
import br.com.zup.academy.alissonprado.model.TipoConta
import br.com.zup.academy.alissonprado.repository.ChavePixRepository
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Singleton
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

@MicronautTest(transactional = false)
internal class RemovePixEndpointTest(
    val grpcClient: RemovePixServiceGrpc.RemovePixServiceBlockingStub,
    val repository: ChavePixRepository
) {

    companion object {
        val CLIENTE_ID = UUID.randomUUID().toString()
        val PIX_ID = UUID.randomUUID().toString()
        val CHAVE = "teste@teste.com"
    }

    @BeforeEach
    fun setUp() {
        repository.deleteAll()
    }

    @Test
    fun `deve remover chave`() {
        // Adiciona registro no banco
        val chavePix = ChavePix(
            idClienteBanco = CLIENTE_ID,
            tipoConta = TipoConta.CONTA_CORRENTE,
            tipoChave = TipoChave.EMAIL,
            chave = CHAVE,
            conta = ContaAssociada(
                instituicaoNome = "Itau",
                instituicaoIspb = "265874",
                nomeDoTitular = "Teste",
                cpfDoTitular = "00000000000",
                agencia = "0001",
                numeroDaConta = "1234"
            )
        )

        val chavePixCadastrada = repository.save(chavePix)

        val response = grpcClient.removePix(
            RemovePixRequest.newBuilder()
                .setIdPix(chavePixCadastrada.idPix)
                .setIdClienteBanco(chavePixCadastrada.idClienteBanco)
                .build()
        )

        assertEquals("Chave ${chavePixCadastrada.chave} removida com sucesso", response.mensagem)
    }

    @Test
    fun `deve dar erro ao tentar remover a chave de outro usuario`() {
        // Adiciona registro no banco
        val chavePixUserA = ChavePix(
            idClienteBanco = CLIENTE_ID,
            tipoConta = TipoConta.CONTA_CORRENTE,
            tipoChave = TipoChave.EMAIL,
            chave = CHAVE,
            conta = ContaAssociada(
                instituicaoNome = "Itau",
                instituicaoIspb = "265874",
                nomeDoTitular = "Teste",
                cpfDoTitular = "00000000000",
                agencia = "0001",
                numeroDaConta = "1234"
            )
        )

        val chavePixUserB = ChavePix(
            idClienteBanco = UUID.randomUUID().toString(),
            tipoConta = TipoConta.CONTA_CORRENTE,
            tipoChave = TipoChave.EMAIL,
            chave = "userB@teste.com",
            conta = ContaAssociada(
                instituicaoNome = "Itau",
                instituicaoIspb = "265874",
                nomeDoTitular = "Teste",
                cpfDoTitular = "00000000000",
                agencia = "0001",
                numeroDaConta = "1234"
            )
        )

        val chavePixCadastradaUserA = repository.save(chavePixUserA)
        val chavePixCadastradaUserB = repository.save(chavePixUserB)

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.removePix(
                RemovePixRequest.newBuilder()
                    .setIdPix(chavePixCadastradaUserB.idPix)
                    .setIdClienteBanco(chavePixCadastradaUserA.idClienteBanco)
                    .build()
            )
        }

        // Validações
        with(error) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave Pix não encontrada", status.description)
        }
    }

    @Test
    fun `deve dar erro NOT_FOUND ao tentar remover chave nao cadastrada`() {

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.removePix(
                RemovePixRequest.newBuilder()
                    .setIdPix(PIX_ID)
                    .setIdClienteBanco(CLIENTE_ID)
                    .build()
            )
        }

        // Validações
        with(error) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave Pix não encontrada", status.description)
        }
    }

    @Test
    fun `deve dar erro ao tentar remover chave enviando atributo idPix vazio`() {

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.removePix(
                RemovePixRequest.newBuilder()
                    .setIdPix("")
                    .setIdClienteBanco(CLIENTE_ID)
                    .build()
            )
        }

        // Validações
        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertTrue(status.description!!.contains("remove.removePixDto.idPix: must not be blank"))
        }
    }

    @Test
    fun `deve dar erro ao tentar remover chave enviando atributo setIdClienteBanco vazio`() {

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.removePix(
                RemovePixRequest.newBuilder()
                    .setIdPix(PIX_ID)
                    .setIdClienteBanco("")
                    .build()
            )
        }

        // Validações
        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
//            assertEquals("Chave Pix não encontrada", status.description)
            assertTrue(status.description!!.contains("Formato de UUID invalido"))
        }
    }

    @Test
    fun `deve dar erro ao tentar remover chave enviando atributo idPix incorreto`() {

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.removePix(
                RemovePixRequest.newBuilder()
                    .setIdPix("80651084-42da-4307-aba2-c84dff01743")
                    .setIdClienteBanco(CLIENTE_ID)
                    .build()
            )
        }

        // Validações
        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertTrue(status.description!!.contains("Formato de UUID invalido"))
        }
    }

    @Test
    fun `deve dar erro ao tentar remover chave enviando atributo setIdClienteBanco incorreto`() {

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.removePix(
                RemovePixRequest.newBuilder()
                    .setIdPix(PIX_ID)
                    .setIdClienteBanco("80651084-42da-4307-aba2-c84dff01743")
                    .build()
            )
        }

        // Validações
        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertTrue(status.description!!.contains("Formato de UUID invalido"))
        }
    }
}

@Factory
class ClientsFactory {

    // channel -> Canal com os dados da requisição
    // GrpcServerChannel.NAME -> Para retornar os dados da conexão, ip e porta gerados pelo contexto de testes do Microunaut
    @Singleton
    fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): RemovePixServiceGrpc.RemovePixServiceBlockingStub {
        return RemovePixServiceGrpc.newBlockingStub(channel)
    }
}