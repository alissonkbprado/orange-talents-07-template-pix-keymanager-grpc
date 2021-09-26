package br.com.zup.academy.alissonprado.Exception

class ChavePixNaoEncontradaException : Exception(){
    override val message: String?
        get() = "Chave Pix não encontrada"
}