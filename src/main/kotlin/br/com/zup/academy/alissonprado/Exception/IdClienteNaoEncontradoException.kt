package br.com.zup.academy.alissonprado.Exception

class IdClienteNaoEncontradoException : Exception(){
    override val message: String?
        get() = "Id do Cliente não encontrado"
}