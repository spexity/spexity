package net.spexity.ui.input

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

data class NumberInput(
    override val name: String,
    override val label: String? = null,
    override val help: String? = null,
    val presentation: NumberPresentation,
    val validation: NumberValidation? = null,
) : Input(
    name = name,
    label = label,
    help = help,
)

data class NumberValidation(
    val min: ValidationValue<Number>? = null,
    val max: ValidationValue<Number>? = null,
)

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = NormalNumber::class, name = "normal"),
    JsonSubTypes.Type(value = CurrencyNumber::class, name = "currency"),
)
sealed interface NumberPresentation

class NormalNumber : NumberPresentation
class CurrencyNumber : NumberPresentation
