package br.com.zup.academy.alissonprado.annotation

import br.com.zup.academy.alissonprado.endpoint.registraPix.RegistraPixDto
import br.com.zup.academy.alissonprado.model.TipoChave
import io.micronaut.core.annotation.AnnotationValue
import io.micronaut.validation.validator.constraints.ConstraintValidator
import io.micronaut.validation.validator.constraints.ConstraintValidatorContext
import jakarta.inject.Singleton
import javax.validation.Constraint
import kotlin.annotation.AnnotationTarget.*

@MustBeDocumented
@Target(CLASS, FIELD, CONSTRUCTOR, TYPE)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [ValidPixValidator::class])
annotation class ValidaPix(
    val message: String = "Chave com formato inválido."
)

@Singleton
class ValidPixValidator : ConstraintValidator<ValidaPix, RegistraPixDto> {

    override fun isValid(
        value: RegistraPixDto?,
        annotationMetadata: AnnotationValue<ValidaPix>,
        context: ConstraintValidatorContext
    ): Boolean {
        if (value?.tipoChave == null) {
            return false
        } else {
            when (value.tipoChave) {
                TipoChave.CPF -> return value.chave.matches("^[0-9]{11}\$".toRegex())

                TipoChave.CELULAR -> return value.chave.matches("^\\+[1-9][0-9]\\d{1,14}\$".toRegex())

                TipoChave.EMAIL -> return value.chave.matches("[a-z0-9!#\$%&'*+/=?^_`{|}~-]+(?:.[a-z0-9!#\$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?".toRegex())

                TipoChave.ALEATORIA -> return true
            }
        }
    }

}
