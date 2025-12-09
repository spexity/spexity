package net.spexity.ui.input

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import jakarta.validation.constraints.NotBlank

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = ChoiceInput::class, name = "choice"),
    JsonSubTypes.Type(value = TextInput::class, name = "text"),
    JsonSubTypes.Type(value = NumberInput::class, name = "number"),
    JsonSubTypes.Type(value = FileInput::class, name = "file"),
)
open class Input(
    @field:NotBlank(message = "Name must not be blank")
    open val name: String,
    open val label: String? = null,
    open val help: String? = null,
)

data class ValidationValue<T>(
    val value: T,
    var messageKey: String? = null,
)