package br.com.zup.academy.alissonprado.model

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.*

internal class ChavePixTest{

    companion object {
        val CLIENTE_ID = UUID.randomUUID().toString()
        val PIX_ID = UUID.randomUUID().toString()
        val CHAVE = "teste@teste.com"
    }

    @Test
    fun `deve retornar true como mesmo objeto`(){
        val chavePixA = ChavePix(
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

        val chavePixB = ChavePix(
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

        assertTrue(chavePixA.equals(chavePixB))
    }

    @Test
    fun `deve retornar false com objetos diferentes (chave)`(){
        val chavePixA = ChavePix(
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

        val chavePixB = ChavePix(
            idClienteBanco = CLIENTE_ID,
            tipoConta = TipoConta.CONTA_CORRENTE,
            tipoChave = TipoChave.EMAIL,
            tipoPessoa = TipoPessoa.PESSOA_FISICA,
            chave = "outroemail@teste.com",
            conta = ContaAssociada(
                instituicaoNome = "Itau",
                instituicaoIspb = "60394079",
                nomeDoTitular = "Teste da Silva",
                documentoDoTitular = "00000000000",
                agencia = "0001",
                numeroDaConta = "1234"
            )
        )

        assertFalse(chavePixA.equals(chavePixB))
    }

}