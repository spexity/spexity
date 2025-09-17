package net.spexity.post

object BodyHtmlSerializer {

    fun render(doc: Doc): String =
        (doc.content ?: emptyList()).joinToString(separator = "") { render(it) }

    private fun render(node: Node): String = when (node) {
        is Paragraph -> renderParagraph(node)
        is Heading -> renderHeading(node)
        is BulletList -> renderBulletList(node)
        is OrderedList -> renderOrderedList(node)
        is ListItem -> renderListItem(node)
        is CodeBlock -> renderCodeBlock(node)
        is Blockquote -> renderBlockquote(node)
        is HorizontalRule -> "<hr/>"
        is Text -> renderText(node)
        is Doc -> render(node)
    }

    private fun renderParagraph(n: Paragraph): String {
        val inner = renderChildren(n.content)
        return "<p>$inner</p>"
    }

    private fun renderHeading(n: Heading): String {
        val level = (n.attrs?.level ?: 1).coerceIn(1, 6)
        val inner = renderChildren(n.content)
        return "<h$level>$inner</h$level>"
    }

    private fun renderBulletList(n: BulletList): String {
        val inner = renderChildren(n.content)
        return "<ul>$inner</ul>"
    }

    private fun renderOrderedList(n: OrderedList): String {
        val inner = renderChildren(n.content)
        return "<ol>$inner</ol>"
    }

    private fun renderListItem(n: ListItem): String {
        val inner = renderChildren(n.content)
        return "<li>$inner</li>"
    }

    private fun renderCodeBlock(n: CodeBlock): String {
        val codeText = renderPlainText(n.content)
        return "<pre><code>$codeText</code></pre>"
    }

    private fun renderBlockquote(n: Blockquote): String {
        val inner = renderChildren(n.content)
        return "<blockquote>$inner</blockquote>"
    }

    private fun renderText(n: Text): String {
        val wrapped = applyMarks(n.text.orEmpty(), n.marks ?: emptyList())
        return wrapped
    }

    private fun renderChildren(children: List<Node>?): String =
        (children ?: emptyList()).joinToString(separator = "") { render(it) }


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

    private fun applyMarks(text: String, marks: List<Mark>): String {
        var out = text
        marks.forEach { mark ->
            out = when (mark) {
                is Bold -> "<strong>$out</strong>"
                is Italic -> "<em>$out</em>"
                is Underline -> "<u>$out</u>"
                is Strike -> "<s>$out</s>"
                is Code -> "<code>$out</code>"
                is Link -> {
                    val href = mark.attrs?.href.orEmpty()
                    "<a href=\"$href\" target=\"_blank\">$out</a>"
                }
            }
        }
        return out
    }


}