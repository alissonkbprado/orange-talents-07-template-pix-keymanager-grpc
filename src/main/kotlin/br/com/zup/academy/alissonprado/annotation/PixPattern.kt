package br.com.zup.academy.alissonprado.annotation

import br.com.zup.academy.alissonprado.endpoint.RegistraPixDto
import br.com.zup.academy.alissonprado.model.TipoChave
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.core.annotation.AnnotationValue
import io.micronaut.validation.validator.constraints.ConstraintValidator
import io.micronaut.validation.validator.constraints.ConstraintValidatorContext
import jakarta.inject.Singleton
import javax.validation.Constraint
import javax.validation.ConstraintViolationException
import kotlin.annotation.AnnotationTarget.*

@MustBeDocumented
@Target(CLASS, FIELD, CONSTRUCTOR)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [PixPatternValidator::class])
annotation class PixPattern(
    val message: String = "Chave com formato inválido."
)

@Singleton
class PixPatternValidator : ConstraintValidator<PixPattern, RegistraPixDto> {

    override fun isValid(
        value: RegistraPixDto?,
        annotationMetadata: AnnotationValue<PixPattern>,
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
