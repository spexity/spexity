package net.spexity.user

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [AvatarBgColorValidator::class])
annotation class ValidAvatarBgColor(
    val message: String = "must be a valid emoji background color",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

val HexRegex = "^#[A-Fa-f0-9]{6}$".toRegex()

class AvatarBgColorValidator : ConstraintValidator<ValidAvatarBgColor, String> {
    override fun isValid(value: String?, context: ConstraintValidatorContext): Boolean {
        return value != null && value.matches(HexRegex)
    }
}
