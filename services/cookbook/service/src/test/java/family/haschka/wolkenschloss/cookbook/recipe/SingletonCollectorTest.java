package family.haschka.wolkenschloss.cookbook.recipe;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

public class SingletonCollectorTest {

    @Test
    public void streamsMustHaveOnlyOneElement() {
        var item = Stream.of(1).collect(SingletonCollector.toItem());
        Assertions.assertEquals(1, item);
    }

    @Test
    public void streamsMustNotBeEmpty() {
        Assertions.assertThrows(SingletonCollector.TooFewItemsException.class, () -> {
            Stream.empty().collect(SingletonCollector.toItem());
        });
    }

    @Test
    public void streamsMustNotHaveMoreThanOneElement() {
        Assertions.assertThrows(SingletonCollector.TooManyItemsException.class, () -> {
            Stream.of(1, 2, 3).collect(SingletonCollector.toItem());
        });
    }
}
