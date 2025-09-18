package net.spexity.post

object BodyTextSerializer {

    fun render(doc: Doc): String =
        (doc.content ?: emptyList()).joinToString(separator = " ") { render(it) }

    private fun render(node: Node): String = when (node) {
        is Paragraph -> renderChildren(node.content)
        is Heading -> renderChildren(node.content)
        is BulletList -> renderChildren(node.content)
        is OrderedList -> renderChildren(node.content)
        is ListItem -> renderListItem(node)
        is CodeBlock -> renderPlainText(node.content)
        is Blockquote -> renderChildren(node.content)
        is HorizontalRule -> " "
        is Text -> node.text.orEmpty()
        is Doc -> render(node)
    }

    private fun renderListItem(n: ListItem): String {
        val inner = renderChildren(n.content)
        return "- $inner"
    }

    private fun renderChildren(children: List<Node>?): String =
        (children ?: emptyList()).joinToString(separator = " ") { render(it) }


    private fun renderPlainText(children: List<Node>?): String {
        val sb = StringBuilder()
        children.orEmpty().forEach { ch ->
            when (ch) {
                is Text -> sb.append(ch.text.orEmpty())
                else -> sb.append(render(ch))
            }
        }
        return sb.toString()
    }

}