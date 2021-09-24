package br.com.zup.academy.alissonprado.httpClient.bcb.cadastraChavePixBcb.dto

data class BankAccount(
    val participant: String, // (ISPB (Identificador de Sistema de Pagamento Brasileiro) do ITAÚ UNIBANCO S.A)
    val branch: String, // (Agência, sem dígito verificador.)
    val accountNumber: String, // (Número de conta, incluindo verificador. Se verificador for letra, substituir por 0.)
    val accountType: AccountType // Tipo de conta (CACC=Conta Corrente; SVGS=Conta Poupança)
) {

}
