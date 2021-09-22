package br.com.zup.academy.alissonprado.converter

import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Value
import java.nio.charset.StandardCharsets
import java.security.Key
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.persistence.AttributeConverter
import javax.persistence.Converter


/**
 * Classe que converte um atributo (em modo texto ) de uma entidade que precisa ser
 * encriptada ao ser perisistida e desencriptada ao ser recuperada do banco de dados.
 *
 * A chave (key) para encriptar é recuperada do arquivo application.properties, que pode ser
 * atribuida via variável de ambiente.
 *
 * fonte: https://thorben-janssen.com/how-to-use-jpa-type-converter-to/
 *
 */
@Converter
class CryptoConverter : AttributeConverter<String, String> {
    @Value("\${encryption_key}")
    private lateinit var encription_key: String

    /**
     * Realiza a encriptação do dado e codifica para Base64
     * @param value
     * @return
     */
    override fun convertToDatabaseColumn(value: String): String {
        val chave: Key = SecretKeySpec(encription_key?.toByteArray(), "AES")
        return try {
            val c = Cipher.getInstance(ALGORITMO)
            c.init(Cipher.ENCRYPT_MODE, chave, GCMParameterSpec(TAG_LENGTH_BIT, IV))
            String(Base64.getEncoder().encode(c.doFinal(value.toByteArray())), StandardCharsets.UTF_8)
        } catch (e: Exception) {
            throw StatusRuntimeException(Status.INTERNAL.withDescription("Falha ao tentar Encriptar."))
        }
    }

    /**
     * Decodifica o dado e realiza a desencriptação
     * @param dbData
     * @return
     */
    override fun convertToEntityAttribute(dbData: String): String {

        val chave: Key = SecretKeySpec(encription_key!!.toByteArray(), "AES")
        return try {
            val c = Cipher.getInstance(ALGORITMO)
            c.init(Cipher.DECRYPT_MODE, chave, GCMParameterSpec(TAG_LENGTH_BIT, IV))
            String(c.doFinal(Base64.getDecoder().decode(dbData.toByteArray())), StandardCharsets.UTF_8)
        } catch (e: Exception) {
            throw StatusRuntimeException(Status.INTERNAL.withDescription("Falha ao tentar Desencriptar."))
        }
    }

    companion object {
        private const val ALGORITMO = "AES/GCM/NoPadding"
        private const val TAG_LENGTH_BIT = 128

        // initialization vector
        private val IV = ByteArray(32)
    }
}
