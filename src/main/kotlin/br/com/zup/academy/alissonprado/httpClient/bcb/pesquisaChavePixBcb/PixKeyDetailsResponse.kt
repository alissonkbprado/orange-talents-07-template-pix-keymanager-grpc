package br.com.zup.academy.alissonprado.httpClient.bcb.pesquisaChavePixBcb

import br.com.zup.academy.alissonprado.endpoint.pesquisaPix.ChavePixInfo
import br.com.zup.academy.alissonprado.httpClient.Instituicoes
import br.com.zup.academy.alissonprado.model.ContaAssociada

import br.com.zup.academy.alissonprado.model.TipoChave
import br.com.zup.academy.alissonprado.model.TipoConta
import java.time.LocalDateTime

data class PixKeyDetailsResponse(
    val keyType: PixKeyType,
    val key: String,
    val bankAccount: BankAccount,
    val owner: Owner,
    val createdAt: LocalDateTime
) {

    fun toModel(): ChavePixInfo {
        return ChavePixInfo(
            tipo = keyType.domainType!!,
            chave = this.key,
            tipoDeConta = when (this.bankAccount.accountType) {
                BankAccount.AccountType.CACC -> TipoConta.CONTA_CORRENTE
                BankAccount.AccountType.SVGS -> TipoConta.CONTA_POUPANCA
            },
            conta = ContaAssociada(
                instituicaoNome = Instituicoes.nome(bankAccount.participant),
                instituicaoIspb = null,
                nomeDoTitular = owner.name,
                documentoDoTitular = owner.taxIdNumber,
                agencia = bankAccount.branch,
                numeroDaConta = bankAccount.accountNumber
            )
        )
    }
}

data class CreatePixKeyResponse(
    val keyType: PixKeyType,
    val key: String,
    val bankAccount: BankAccount,
    val owner: Owner,
    val createdAt: LocalDateTime
)

data class Owner(
    val type: OwnerType,
    val name: String,
    val taxIdNumber: String
) {

    enum class OwnerType {
        NATURAL_PERSON,
        LEGAL_PERSON
    }
}

data class BankAccount(
    /**
     * 60701190 ITAÚ UNIBANCO S.A.
     * https://www.bcb.gov.br/pom/spb/estatistica/port/ASTR003.pdf (line 221)
     */
    val participant: String,
    val branch: String,
    val accountNumber: String,
    val accountType: AccountType
) {

    /**
     * https://open-banking.pass-consulting.com/json_ExternalCashAccountType1Code.html
     */
    enum class AccountType() {

        CACC, // Current: Account used to post debits and credits when no specific account has been nominated
        SVGS; // Savings: Savings

        companion object {
            fun by(domainType: TipoConta): AccountType {
                return when (domainType) {
                    TipoConta.CONTA_CORRENTE -> CACC
                    TipoConta.CONTA_POUPANCA -> SVGS
                }
            }
        }
    }

}

enum class PixKeyType(val domainType: TipoChave) {

    CPF(TipoChave.CPF),
    PHONE(TipoChave.CELULAR),
    EMAIL(TipoChave.EMAIL),
    RANDOM(TipoChave.ALEATORIA);

    companion object {

        private val mapping = PixKeyType.values().associateBy(PixKeyType::domainType)

        fun by(domainType: TipoChave): PixKeyType {
            return  mapping[domainType] ?: throw IllegalArgumentException("PixKeyType invalid or not found for $domainType")
        }
    }
}