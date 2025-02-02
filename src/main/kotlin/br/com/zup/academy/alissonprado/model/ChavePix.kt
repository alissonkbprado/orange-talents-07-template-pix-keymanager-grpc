package br.com.zup.academy.alissonprado.model

import br.com.zup.academy.alissonprado.validation.ValidaUUID
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Entity
class ChavePix(
    @field:NotBlank @field:ValidaUUID
    @Column(nullable = false)
    val idClienteBanco: String,

    @field:NotNull @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val tipoChave: TipoChave,

    @field:NotNull @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val tipoConta: TipoConta,

    @field:NotNull @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val tipoPessoa: TipoPessoa,

    @field:NotBlank @field:Size(max = 77)
    @Column(nullable = false, unique = true)
//    @Convert(converter = CryptoConverter::class)
    var chave: String,

    @field:Valid
    @Embedded
    val conta: ContaAssociada
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    @Column(nullable = false, unique = true)
    val idPix: String = UUID.randomUUID().toString()

    @Column(nullable = false)
    val criadaEm: LocalDateTime = LocalDateTime.now()

    /**
     * Verifica se esta chave pertence a este cliente
     */
    fun pertenceAo(clienteId: String) = this.idClienteBanco.equals(clienteId)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ChavePix

        if (idClienteBanco != other.idClienteBanco) return false
        if (tipoChave != other.tipoChave) return false
        if (tipoConta != other.tipoConta) return false
        if (chave != other.chave) return false

        return true
    }

    override fun hashCode(): Int {
        var result = idClienteBanco.hashCode()
        result = 31 * result + tipoChave.hashCode()
        result = 31 * result + tipoConta.hashCode()
        result = 31 * result + (chave?.hashCode() ?: 0)
        return result
    }

    fun adicionaChaveBancoCentral(key: String?) {
        if (key != null) {
            this.chave = key
        }
    }
}
