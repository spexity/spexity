package net.spexity.ui.input

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

data class TextInput(
    override val name: String,
    override val label: String? = null,
    override val help: String? = null,
    val presentation: TextPresentation,
    val validation: TextValidation? = null,
) : Input(
    name = name,
    label = label,
    help = help,
)

data class TextValidation(
    val minLength: ValidationValue<Int>? = null,
    val maxLength: ValidationValue<Int>? = null,
)

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = TextLine::class, name = "textLine"),
    JsonSubTypes.Type(value = TextBox::class, name = "textBox"),
)
sealed interface TextPresentation

class TextLine : TextPresentation
class TextBox : TextPresentation