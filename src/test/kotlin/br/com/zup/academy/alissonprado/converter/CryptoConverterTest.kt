package br.com.zup.academy.alissonprado.converter

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

@MicronautTest(transactional = false)
internal class CryptoConverterTest{

    @Inject
    lateinit var cryptoConverter: CryptoConverter

    @Test
    fun `deve encriptar corretamente a string passada`(){
        val texto = "Teste de String"

        val textoEncriptado = cryptoConverter.convertToDatabaseColumn(texto)

        assertNotEquals(texto, textoEncriptado)
    }

    @Test
    fun `deve encriptar e retornar o mesmo valor para a mesma string passada`(){
        val texto = "Teste de String"

        val textoEncriptado = cryptoConverter.convertToDatabaseColumn(texto)
        val textoEncriptado2 = cryptoConverter.convertToDatabaseColumn(texto)

        assertEquals(textoEncriptado, textoEncriptado2)
    }

    @Test
    fun `deve desencriptar corretamente string encriptada passada`(){
        val texto = "Teste de String"

        val textoEncriptado = cryptoConverter.convertToDatabaseColumn(texto)

        val textoDesencriptado = cryptoConverter.convertToEntityAttribute(textoEncriptado)

        assertEquals(textoDesencriptado, texto)
    }

    @Test
    fun `deve desencriptar e retornar o mesmo valor para a mesma string encriptada passada`(){
        val texto = "Teste de String"

        val textoEncriptado = cryptoConverter.convertToDatabaseColumn(texto)

        val textoDesencriptado = cryptoConverter.convertToEntityAttribute(textoEncriptado)
        val textoDesencriptado2 = cryptoConverter.convertToEntityAttribute(textoEncriptado)

        assertEquals(textoDesencriptado, textoDesencriptado2)
    }
}