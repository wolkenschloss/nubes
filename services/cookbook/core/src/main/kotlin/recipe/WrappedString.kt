package family.haschka.wolkenschloss.cookbook.recipe

data class WrappedString(val text: String) {

    fun wrap(i: Int): String {
        val paragraphs = text.split("\n\n")
        return paragraphs.joinToString("\n\n") { p -> wrapParagraph(p, i) }
    }

    private fun wrapParagraph(paragraph: String, i: Int): String {
        val regex = "\\s+".toRegex()
        val x = paragraph.split(regex)

        val builder = StringBuilder()

        var row = 0
        var pos = 0
        for (idx: Int in x.indices) {

            // Wort hinzufügen
            builder.append(x[idx])
            row += x[idx].length
            pos += x[idx].length

            // Leerzeichen hinzufügen, wenn noch weitere Wörter folgen,
            // die nicht zu einem Zeilenumbruch führen.
            if (idx + 1 < x.size) {
                if (row + 1 + x[idx + 1].length > i) {
                    builder.append("\n")
                    row = 0
                } else {
                    builder.append(" ")
                    row += 1
                    pos += 1
                }
            }
        }

        return builder.toString()
    }
}
