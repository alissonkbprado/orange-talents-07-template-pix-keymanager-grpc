package br.com.zup.academy.alissonprado.endpoint.removePix

import br.com.zup.academy.alissonprado.Exception.ChaveNaoEncontradaException
import br.com.zup.academy.alissonprado.Exception.ChaveNaoPertenceAoUsuarioException
import br.com.zup.academy.alissonprado.model.ChavePix
import br.com.zup.academy.alissonprado.model.ContaAssociada
import br.com.zup.academy.alissonprado.model.TipoChave
import br.com.zup.academy.alissonprado.model.TipoConta
import br.com.zup.academy.alissonprado.repository.ChavePixRepository
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

@MicronautTest(transactional = false)
internal class RemovePixServiceTest(
    val service: RemovePixService,
    val repository: ChavePixRepository
) {
    companion object {
        val CLIENTE_ID = UUID.randomUUID().toString()
        val CHAVE = "teste@teste.com"
    }

    @BeforeEach
    fun setUp() {
        repository.deleteAll()
    }

    @Test
    fun `deve remover chave cadastrada`() {
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

        val response = service.remove(
            RemovePixDto(
                idPix = chavePixCadastrada.idPix,
                idClienteBanco = chavePixCadastrada.idClienteBanco
            )
        )

        assertEquals("Chave ${chavePixCadastrada.chave} removida com sucesso", response)
    }

    @Test
    fun `deve dar erro ao tentar remover chave nao cadastrada`() {

        val erro = assertThrows<ChaveNaoPertenceAoUsuarioException> {
            service.remove(
                RemovePixDto(
                    idPix = UUID.randomUUID().toString(),
                    idClienteBanco = CLIENTE_ID
                )
            )
        }

        assertEquals(null, erro.message)
    }

    @Test
    fun `deve dar erro ao passar RemovePixDto com idPix invalido`() {

        val erro = assertThrows<IllegalArgumentException> {
            service.remove(
                RemovePixDto(
                    idPix = "123",
                    idClienteBanco = CLIENTE_ID
                )
            )
        }

        assertEquals("Invalid UUID string: 123", erro.message)
    }

    @Test
    fun `deve dar erro ao passar RemovePixDto com idClienteBanco invalido`() {

        val erro = assertThrows<IllegalArgumentException> {
            service.remove(
                RemovePixDto(
                    idPix = UUID.randomUUID().toString(),
                    idClienteBanco = "123",
                )
            )
        }

        assertEquals("Invalid UUID string: 123", erro.message)
    }
}