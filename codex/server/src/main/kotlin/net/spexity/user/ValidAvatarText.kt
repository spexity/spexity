package net.spexity.user

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import java.text.BreakIterator
import java.util.*
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [AvatarTextValidator::class])
annotation class ValidAvatarText(
    val message: String = "must be emojis",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []

)

class AvatarTextValidator : ConstraintValidator<ValidAvatarText, String> {

    override fun isValid(value: String?, context: ConstraintValidatorContext): Boolean {
        if (value == null) return false
        val countEmojis = countEmojis(value)
        return countEmojis == 2;
    }

    fun countEmojis(input: String): Int {
        val it = BreakIterator.getCharacterInstance(Locale.ENGLISH)
        it.setText(input)

        var count = 0
        var start = it.first()
        var end = it.next()
        while (end != BreakIterator.DONE) {
            val grapheme = input.substring(start, end)
            if (isEmoji(grapheme)) {
                count++
            }
            start = end
            end = it.next()
        }
        return count
    }

    private fun isEmoji(grapheme: String): Boolean {
        return grapheme.codePoints().anyMatch { cp: Int ->
            Character.UnicodeScript.of(cp) == Character.UnicodeScript.COMMON &&
                    Character.getType(cp) == Character.OTHER_SYMBOL.toInt()
        }
    }
}
