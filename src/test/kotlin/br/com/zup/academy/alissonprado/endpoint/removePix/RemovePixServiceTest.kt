package br.com.zup.academy.alissonprado.endpoint.removePix

import br.com.zup.academy.alissonprado.Exception.ChaveNaoEncontradaException
import br.com.zup.academy.alissonprado.Exception.ChavePixNaoEncontradaBCBException
import br.com.zup.academy.alissonprado.httpClient.bcb.cadastraChavePixBcb.CadastraChavePixBcbClient
import br.com.zup.academy.alissonprado.httpClient.bcb.removeChavePixBcb.RemoveChavePixBcbClient
import br.com.zup.academy.alissonprado.httpClient.bcb.removeChavePixBcb.dto.RemovePixKeyRequest
import br.com.zup.academy.alissonprado.httpClient.bcb.removeChavePixBcb.dto.RemovePixKeyResponse
import br.com.zup.academy.alissonprado.model.*
import br.com.zup.academy.alissonprado.repository.ChavePixRepository
import com.github.dockerjava.zerodep.shaded.org.apache.hc.client5.http.HttpResponseException
import io.micronaut.http.HttpResponse
import io.micronaut.http.client.exceptions.HttpClientException
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import java.net.ConnectException
import java.time.LocalDateTime
import java.util.*

@MicronautTest(transactional = false)
internal class RemovePixServiceTest(
    val service: RemovePixService,
    val repository: ChavePixRepository,
) {
    companion object {
        val CLIENTE_ID = UUID.randomUUID().toString()
        val CHAVE = "teste@teste.com"
    }

    @field:Inject
    lateinit var bcbClient: RemoveChavePixBcbClient

    private lateinit var chavePix: ChavePix

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
                instituicaoIspb = "265874",
                nomeDoTitular = "Teste",
                documentoDoTitular = "00000000000",
                agencia = "0001",
                numeroDaConta = "1234"
            )
        )
    }

    @Test
    fun `deve remover chave cadastrada`() {
        val chavePixCadastrada = repository.save(chavePix)

        Mockito.`when`(
            bcbClient.remove(
                chavePix.chave, RemovePixKeyRequest(
                    key = chavePix.chave,
                    participant = chavePix.conta.instituicaoIspb
                )
            )
        ).thenReturn(
            HttpResponse.ok(
                RemovePixKeyResponse(
                    key = chavePix.chave,
                    participant = chavePix.conta.instituicaoIspb,
                    deletedAt = LocalDateTime.now().toString()
                )
            )
        )

        val response = service.remove(
            RemovePixDto(
                idPix = chavePixCadastrada.idPix,
                idClienteBanco = chavePixCadastrada.idClienteBanco
            )
        )

        assertEquals("Chave ${chavePixCadastrada.chave} removida com sucesso", response)
    }

    @Test
    fun `nao deve remover chave cadastrada no Banco Central se campo participant-instituicaoIspb estiver incorreto`() {
        val chavePixCadastrada = repository.save(chavePix)

        Mockito.`when`(
            bcbClient.remove(
                chavePix.chave, RemovePixKeyRequest(
                    key = chavePix.chave,
                    participant = "VALOR_INCORRETO"
                )
            )
        ).thenReturn(
            HttpResponse.notAllowed()
        )

        val response = assertThrows<IllegalArgumentException> {
            service.remove(
                RemovePixDto(
                    idPix = chavePixCadastrada.idPix,
                    idClienteBanco = chavePixCadastrada.idClienteBanco
                )
            )
        }

        assertEquals("Falha ao tentar remover chave Pix no Banco Central", response.message)
    }

    @Test
    fun `nao deve remover chave se nao estiver cadastrada no Banco Central`() {
        val chavePixCadastrada = repository.save(chavePix)

        Mockito.`when`(
            bcbClient.remove(
                chavePix.chave, RemovePixKeyRequest(
                    key = chavePix.chave,
                    participant = chavePix.conta.instituicaoIspb
                )
            )
        ).thenReturn(
            HttpResponse.notFound()
        )

        val response = assertThrows<ChavePixNaoEncontradaBCBException> {
            service.remove(
                RemovePixDto(
                    idPix = chavePixCadastrada.idPix,
                    idClienteBanco = chavePixCadastrada.idClienteBanco
                )
            )
        }

        assertEquals("Chave Pix não encontrada no Banco Central", response.message)
    }

    @Test
    fun `deve lancar exception se sistema do Banco Central estiver indisponivel`() {
        val chavePixCadastrada = repository.save(chavePix)

        Mockito.`when`(
            bcbClient.remove(
                chavePix.chave, RemovePixKeyRequest(
                    key = chavePix.chave,
                    participant = chavePix.conta.instituicaoIspb
                )
            )
        ).thenThrow(
            HttpClientException("Connect Error: Connection refused: no further information: localhost/127.0.0.1:9091")
        )

        val response = assertThrows<HttpClientException> {
            service.remove(
                RemovePixDto(
                    idPix = chavePixCadastrada.idPix,
                    idClienteBanco = chavePixCadastrada.idClienteBanco
                )
            )
        }

        assertEquals("Não foi possível acessar o sistema do Banco Central", response.message)
    }

    @Test
    fun `deve dar erro ao tentar remover chave nao cadastrada`() {

        val erro = assertThrows<ChaveNaoEncontradaException> {
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
    fun `deve dar erro ao tentar RemovePixDto com idPix invalido`() {

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
    fun `deve dar erro ao tentar RemovePixDto com idClienteBanco invalido`() {

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

    @MockBean(RemoveChavePixBcbClient::class)
    fun removeChaveBcbMock(): RemoveChavePixBcbClient {
        return Mockito.mock(RemoveChavePixBcbClient::class.java)
    }

}

