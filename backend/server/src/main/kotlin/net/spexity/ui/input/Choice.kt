package net.spexity.ui.input

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

//Todo: what about 'creatable'. how to handle that?
data class ChoiceInput(
    override val name: String,
    override val label: String? = null,
    override val help: String? = null,
    val options: Options,
    val presentation: ChoicePresentation,
    val validation: ChoiceValidation? = null,
) : Input(
    name = name,
    label = label,
    help = help,
)

/*
* How to Input Table Example*
 	Strongly Disagree	Disagree	Neutral	Agree	Strongly Agree
Statement 1		x
Statement 2								x
Statement 3												x
* */

/*
How to: rating stars.
*
 */

data class ChoiceValidation(
    // For example, "Accept terms and conditions" has minSelections 1.
    // A required choice will also have minSelections 1.
    // maxSelections dictates if multiple items can be selected.
    val requiredSelections: ValidationValue<List<String>>? = null,
    val minSelections: ValidationValue<Int>? = null,
    val maxSelections: ValidationValue<Int>? = null,
)

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = ChoiceTable::class, name = "table"),
    JsonSubTypes.Type(value = ChoiceCheckboxGroup::class, name = "checkboxGroup"),
    JsonSubTypes.Type(value = ChoiceRadioGroup::class, name = "radioGroup"),
)
sealed interface ChoicePresentation

class ChoiceTable : ChoicePresentation
class ChoiceCheckboxGroup : ChoicePresentation
class ChoiceRadioGroup : ChoicePresentation

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = InlineOptions::class, name = "inline"),
    JsonSubTypes.Type(value = RemoteOptions::class, name = "remote"),
    JsonSubTypes.Type(value = PredefinedSetOptions::class, name = "predefinedSet"),
)
sealed interface Options

data class InlineOptions(
    val options: List<Option>,
) : Options

data class RemoteOptions(
    val url: String,
) : Options


data class PredefinedSetOptions(
    val setId: String,
) : Options

data class Option(
    val id: String,
    //How to represnt things like label, description, but maybe some times accept icons as well.
    //icon can also have source: url, source: emoji, idk.
    val data: Any? = null,
)