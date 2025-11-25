package input

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import jakarta.validation.Valid

//Todo: what about 'creatable'. how to handle that?
data class ChoiceInput(
    @field:Valid val options: Options,
    @field:Valid val presentation: Presentation,
    @field:Valid val validation: Validation,
)

data class Validation(
    // For example, "Accept terms and conditions" has minSelections 1.
    // A required choice will also have minSelections 1.
    // maxSelections dictates if multiple items can be selected.
    val minSelections: ValidationValue<Int>,
    val maxSelections: ValidationValue<Int>?,
)

data class ValidationValue<T>(
    val value: T,
    val messageKey: String?,
)

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = Table::class, name = "table"),
    JsonSubTypes.Type(value = CheckboxGroup::class, name = "checkboxGroup"),
    JsonSubTypes.Type(value = RadioGroup::class, name = "radioGroup"),
)
sealed interface Presentation

class Table : Presentation
class CheckboxGroup : Presentation
class RadioGroup : Presentation

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = InlineOptions::class, name = "inline"),
    JsonSubTypes.Type(value = RemoteOptions::class, name = "remote"),
    JsonSubTypes.Type(value = SetOptions::class, name = "set"),
)
sealed interface Options

data class InlineOptions(
    val options: List<Option>
) : Options

data class RemoteOptions(
    val url: String
) : Options


data class SetOptions(
    val setId: String
) : Options

data class Option(
    val id: String,
    //How to represnt things like label, description, but maybe some times accept icons as well.
    //icon can also have source: url, source: emoji, idk.
    val data: Any
)