package br.com.zup.academy.alissonprado.Exception

class ChavePixNaoEncontradaBCBException : Exception() {
    override val message: String?
        get() = "Chave Pix não encontrada no Banco Central"
}
