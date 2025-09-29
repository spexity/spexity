package net.spexity.post

object DocumentToTextSerializer {

    fun serialize(document: Document): String =
        (document.content ?: emptyList()).joinToString(separator = " ") { serialize(it) }

    private fun serialize(node: Node): String = when (node) {
        is Paragraph -> renderChildren(node.content)
        is Heading -> renderChildren(node.content)
        is BulletList -> renderChildren(node.content)
        is OrderedList -> renderChildren(node.content)
        is ListItem -> renderListItem(node)
        is CodeBlock -> renderPlainText(node.content)
        is Blockquote -> renderChildren(node.content)
        is HorizontalRule -> " "
        is Text -> node.text.orEmpty()
        is Document -> serialize(node)
    }

    private fun renderListItem(n: ListItem): String {
        val inner = renderChildren(n.content)
        return "- $inner"
    }

    private fun renderChildren(children: List<Node>?): String =
        (children ?: emptyList()).joinToString(separator = " ") { serialize(it) }


    private fun renderPlainText(children: List<Node>?): String {
        val sb = StringBuilder()
        children.orEmpty().forEach { ch ->
            when (ch) {
                is Text -> sb.append(ch.text.orEmpty())
                else -> sb.append(serialize(ch))
            }
        }
        return sb.toString()
    }

}