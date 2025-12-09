package net.spexity.ui.input

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

data class FileInput(
    override val name: String,
    override val label: String? = null,
    override val help: String? = null,
    val presentation: FilePresentation,
    val validation: FileValidation? = null,
) : Input(
    name = name,
    label = label,
    help = help,
)

data class FileValidation(
    val extensions: ValidationValue<List<String>>? = null,
    val minSize: ValidationValue<Long>? = null,
    val maxSize: ValidationValue<Long>? = null,
)

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = FileBox::class, name = "fileBox"),
    JsonSubTypes.Type(value = SignatureBox::class, name = "signature"),
)
sealed interface FilePresentation

class FileBox : FilePresentation

class SignatureBox : FilePresentation