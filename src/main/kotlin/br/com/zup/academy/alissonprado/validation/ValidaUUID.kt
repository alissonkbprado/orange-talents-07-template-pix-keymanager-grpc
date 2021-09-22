package br.com.zup.academy.alissonprado.validation

import io.micronaut.core.annotation.AnnotationValue
import io.micronaut.validation.validator.constraints.ConstraintValidator
import io.micronaut.validation.validator.constraints.ConstraintValidatorContext
import jakarta.inject.Singleton
import java.util.*
import javax.validation.Constraint
import javax.validation.Payload
import kotlin.annotation.AnnotationRetention.*
import kotlin.annotation.AnnotationTarget.*
import kotlin.reflect.KClass

@MustBeDocumented
@Constraint(validatedBy = [UuidValidator::class])
@Retention(RUNTIME)
@Target(FIELD, CONSTRUCTOR, PROPERTY, VALUE_PARAMETER)
annotation class ValidaUUID(
    val message: String = "Formato de UUID invalido",
    val groups: Array<KClass<Any>> = [],
    val payload: Array<KClass<Payload>> = [],
)

@Singleton
class UuidValidator : ConstraintValidator<ValidaUUID, String> {

    override fun isValid(
        value: String?,
        annotationMetadata: AnnotationValue<ValidaUUID>,
        context: ConstraintValidatorContext
    ): Boolean {
        if (value.isNullOrBlank())
            return false

        return UUID.fromString(value).toString() == value
    }

}