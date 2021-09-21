package br.com.zup.academy.alissonprado.model

import br.com.zup.academy.alissonprado.validation.ValidUUID
import br.com.zup.academy.alissonprado.converter.CryptoConverter
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Entity
class ChavePix(
    @field:NotBlank @field:ValidUUID
    @Column(nullable = false)
    @Convert(converter = CryptoConverter::class)
    val idClienteBanco: String,

    @field:NotNull @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val tipoChave: TipoChave,

    @field:NotNull @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val tipoConta: TipoConta,

    @field:NotBlank @field:Size(max = 77)
    @Column(nullable = false, unique = true)
    @Convert(converter = CryptoConverter::class)
    val chave: String,

    @field:Valid
    @Embedded
    val conta: ContaAssociada?
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    @Column(nullable = false, unique = true)
    val idPix: String = UUID.randomUUID().toString()

    @Column(nullable = false)
    val criadaEm: LocalDateTime = LocalDateTime.now()

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

    override fun toString(): String {
        return "ChavePix(idClienteBanco='$idClienteBanco', tipoChave=$tipoChave, tipoConta=$tipoConta, chave=$chave, id=$id, idPix='$idPix', criadaEm=$criadaEm)"
    }


}
