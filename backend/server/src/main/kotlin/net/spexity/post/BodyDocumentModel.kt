package net.spexity.post

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = Document::class, name = "doc"),
    JsonSubTypes.Type(value = Paragraph::class, name = "paragraph"),
    JsonSubTypes.Type(value = Text::class, name = "text"),
    JsonSubTypes.Type(value = Heading::class, name = "heading"),
    JsonSubTypes.Type(value = BulletList::class, name = "bulletList"),
    JsonSubTypes.Type(value = OrderedList::class, name = "orderedList"),
    JsonSubTypes.Type(value = ListItem::class, name = "listItem"),
    JsonSubTypes.Type(value = CodeBlock::class, name = "codeBlock"),
    JsonSubTypes.Type(value = Blockquote::class, name = "blockquote"),
    JsonSubTypes.Type(value = HorizontalRule::class, name = "horizontalRule"),
)
sealed interface Node

data class Document(
    val content: List<Node>? = null,
) : Node

data class Heading(
    val attrs: HeadingAttrs? = null,
    val content: List<Node>? = null,
) : Node {
    data class HeadingAttrs(val level: Int? = null)
}

data class Paragraph(
    val content: List<Node>? = null,
) : Node

data class BulletList(
    val content: List<Node>? = null,
) : Node

data class OrderedList(
    val content: List<Node>? = null,
) : Node

data class ListItem(
    val content: List<Node>? = null,
) : Node

data class CodeBlock(
    val attrs: CodeBlockAttrs? = null,
    val content: List<Node>? = null,
) : Node {
    data class CodeBlockAttrs(val language: String? = null)
}

data class Blockquote(
    val content: List<Node>? = null // usually paragraphs
) : Node

class HorizontalRule : Node

data class Text(
    val text: String? = null,
    val marks: List<Mark>? = null,
) : Node

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = Bold::class, name = "bold"),
    JsonSubTypes.Type(value = Italic::class, name = "italic"),
    JsonSubTypes.Type(value = Code::class, name = "code"),
    JsonSubTypes.Type(value = Link::class, name = "link"),
    JsonSubTypes.Type(value = Underline::class, name = "underline"),
    JsonSubTypes.Type(value = Strike::class, name = "strike"),
)

sealed interface Mark

class Bold : Mark
class Italic : Mark
class Code : Mark
class Underline : Mark
class Strike : Mark
data class Link(
    val attrs: LinkAttrs? = null
) : Mark {
    data class LinkAttrs(
        val href: String? = null,
        val target: String? = null,
        val rel: String? = null,
    )
}