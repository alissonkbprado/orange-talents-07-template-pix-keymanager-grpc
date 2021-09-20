package br.com.zup.academy.alissonprado.repository

import br.com.zup.academy.alissonprado.model.ChavePix
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface ChavePixRepository : JpaRepository<ChavePix, Long> {
    fun existsByChave(chave: String?): Boolean
    fun findByIdPix(idPix: String?): Optional<ChavePix>
}