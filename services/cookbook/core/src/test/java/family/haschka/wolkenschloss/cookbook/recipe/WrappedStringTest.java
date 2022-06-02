package family.haschka.wolkenschloss.cookbook.recipe;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

@SuppressWarnings("SpellCheckingInspection")
public class WrappedStringTest {

    @Test
    public void shouldWrapText() {
        final String text = """
                Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy
                eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam
                voluptua. At vero eos et accusam et
                """;

        //               1         2         3
        //      123456789012345678901234567890
        final String expected = """
                Lorem ipsum dolor sit amet,
                consetetur sadipscing elitr,
                sed diam nonumy eirmod tempor
                invidunt ut labore et dolore
                magna aliquyam erat, sed diam
                voluptua. At vero eos et
                accusam et""";
        var wrapped = new WrappedString(text);

        Arrays.stream(wrapped.wrap(30).split("\\n")).forEach(line ->
                Assertions.assertTrue(
                        line.length() < 31,
                        () -> String.format("Zeile '%s' ist zu lang: %d ", line, line.length())));

        Assertions.assertEquals(expected, wrapped.wrap(30));
    }

    @Test
    public void shouldNotWrapIfWordIsTooLong() {
        final String text = "Loremipsumdolorsit amet,consetetursadipscingelitr, sed diam nonumy";

        var wrapped = new WrappedString(text);
        //      1234567890
        final String expected = """
                Loremipsumdolorsit
                amet,consetetursadipscingelitr,
                sed diam
                nonumy""";

        Assertions.assertEquals(expected, wrapped.wrap(10));
    }

    @Test
    public void shouldPreserveMarkdownParagraphs() {
        final String text = """
                Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy
                eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam
                voluptua. At vero eos et accusam et
                
                Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy
                eirmod tempor invidunt ut labore et dolore magna aliquyam erat.            
                """;

        var wrapped = new WrappedString(text);

        //               1         2         3         4
        //      1234567890123456789012345678901234567890
        final String  expected = """
                Lorem ipsum dolor sit amet, consetetur
                sadipscing elitr, sed diam nonumy eirmod
                tempor invidunt ut labore et dolore
                magna aliquyam erat, sed diam voluptua.
                At vero eos et accusam et
                
                Lorem ipsum dolor sit amet, consetetur
                sadipscing elitr, sed diam nonumy eirmod
                tempor invidunt ut labore et dolore
                magna aliquyam erat.""";

        Assertions.assertEquals(expected, wrapped.wrap(40));
    }
}
