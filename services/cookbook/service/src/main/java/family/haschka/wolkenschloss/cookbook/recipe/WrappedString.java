package family.haschka.wolkenschloss.cookbook.recipe;

import java.util.Arrays;
import java.util.stream.Collectors;

public record WrappedString(String text) {

    public String wrap(int i) {
        var paragraphs = text.split("\n\n");

        return Arrays.stream(paragraphs)
                .map(p -> wrapParagraph(p, i))
                .collect(Collectors.joining("\n\n"));
    }

    private String wrapParagraph(String paragraph, int i) {
        var x = paragraph.split("\\s+");

        StringBuilder builder = new StringBuilder();

        int row = 0;
        int pos = 0;
        for (int idx = 0; idx < x.length; idx++) {

            // Wort hinzufügen
            builder.append(x[idx]);
            row = row + x[idx].length();
            pos = pos + x[idx].length();

            // Leerzeichen hinzufügen, wenn noch weitere Wörter folgen,
            // die nicht zu einem Zeilenumbruch führen.
            if (idx + 1 < x.length) {
                if (row + 1 + x[idx + 1].length()  > i) {
                    builder.append("\n");
                    row = 0;
                } else {
                    builder.append(" ");
                    row = row + 1;
                    pos = pos + 1;
                }
            }
        }

        return builder.toString();
    }
}
