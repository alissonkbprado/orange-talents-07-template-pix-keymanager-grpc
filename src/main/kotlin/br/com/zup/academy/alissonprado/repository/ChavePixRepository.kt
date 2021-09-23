package br.com.zup.academy.alissonprado.repository

import br.com.zup.academy.alissonprado.model.ChavePix
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository

@Repository
interface ChavePixRepository : JpaRepository<ChavePix, Long> {
    fun existsByChave(chave: String?): Boolean
    fun findByIdPix(idPix: String?): ChavePix?
    fun existsByIdClienteBancoAndIdPix(idClienteBanco: String, idPix: String): Boolean
    fun existsByIdPixAndIdClienteBanco(idPix: String, idClienteBanco: String): Boolean
    fun findByIdPixAndIdClienteBanco(idPix: String, idClienteBanco: String): ChavePix?
}