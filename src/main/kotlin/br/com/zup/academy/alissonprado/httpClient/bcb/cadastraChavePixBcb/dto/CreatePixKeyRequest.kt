package br.com.zup.academy.alissonprado.httpClient.bcb.cadastraChavePixBcb.dto

data class CreatePixKeyRequest(
    val keyType: KeyType,
    val key: String?,
    val bankAccount: BankAccount,
    val owner: Owner
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CreatePixKeyRequest

        if (keyType != other.keyType) return false
        if (key != other.key) return false

        return true
    }

    override fun hashCode(): Int {
        var result = keyType.hashCode()
        result = 31 * result + (key?.hashCode() ?: 0)
        return result
    }
}
