package br.com.zup.academy.alissonprado.Exception

class ChavePixJaRegistradaBCBException : Exception() {
    override val message: String?
        get() = "Chave Pix já registrada no Banco Central"



}
