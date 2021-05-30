package family.haschka.mycloud.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FirstUnitTest {

    @Test
    void ersterTest() {
        Assertions.assertEquals(1, 1);
    }

    @Test
    void zweiterTest() {
        Assertions.assertTrue(true);
    }

    @Test
    void dritterTest() {
        Assertions.assertEquals("Hello", "Hello");
    }
}
