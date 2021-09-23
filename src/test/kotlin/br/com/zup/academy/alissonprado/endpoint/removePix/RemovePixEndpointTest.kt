package br.com.zup.academy.alissonprado.endpoint.removePix


import br.com.zup.academy.alissonprado.RemovePixRequest
import br.com.zup.academy.alissonprado.RemovePixServiceGrpc
import br.com.zup.academy.alissonprado.model.ChavePix
import br.com.zup.academy.alissonprado.model.ContaAssociada
import br.com.zup.academy.alissonprado.model.TipoChave
import br.com.zup.academy.alissonprado.model.TipoConta
import br.com.zup.academy.alissonprado.repository.ChavePixRepository
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

@MicronautTest(transactional = false)
internal class RemovePixEndpointTest(
//    val removePixGrpcClient: RemovePixServiceGrpc.RemovePixServiceBlockingStub,
    val repository: ChavePixRepository
) {

//    companion object {
//        val CLIENTE_ID = UUID.randomUUID().toString()
//        val CHAVE = "teste@teste.com"
//    }
//
//    @BeforeEach
//    fun setUp() {
//        repository.deleteAll()
//    }
//
//    @Test
//    fun `deve remover chave`() {
//        // Adiciona registro no banco
//        val chavePix = ChavePix(
//            idClienteBanco = CLIENTE_ID,
//            tipoConta = TipoConta.CONTA_CORRENTE,
//            tipoChave = TipoChave.EMAIL,
//            chave = CHAVE,
//            conta = ContaAssociada(
//                instituicaoNome = "Itau",
//                instituicaoIspb = "265874",
//                nomeDoTitular = "Teste",
//                cpfDoTitular = "00000000000",
//                agencia = "0001",
//                numeroDaConta = "1234"
//            )
//        )
//
//        val chavePixCadastrada = repository.save(chavePix)
//
//        val response = removePixGrpcClient.removePix(
//            RemovePixRequest.newBuilder()
//                .setIdPix(chavePixCadastrada.idPix)
//                .setIdClienteBanco(chavePixCadastrada.idClienteBanco)
//                .build()
//        )
//
//        assertEquals("Chave ${chavePixCadastrada.chave} removida com sucesso", "response.mensagem")
//    }
}