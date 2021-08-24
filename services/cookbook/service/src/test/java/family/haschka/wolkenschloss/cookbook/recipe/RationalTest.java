package family.haschka.wolkenschloss.cookbook.recipe;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

public class RationalTest {

    enum Testcase {

        TWO_QUARTERS(new Rational(2, 4), new Rational(1, 2), "1/2"),
        FOUR_QUARTERS(new Rational(4, 4), new Rational(1, 1 ), "1");

        Testcase(Rational actual, Rational expected, String pretty) {
            this.actual = actual;
            this.expected = expected;
            this.pretty = pretty;
        }

        public final Rational actual;
        public final Rational expected;
        private final String pretty;
    }

    @ParameterizedTest
    @EnumSource(Testcase.class)
    public void rationalShouldBeShortened(Testcase testcase) {
        Assertions.assertEquals(testcase.expected, testcase.actual);
    }


    @ParameterizedTest
    @EnumSource(Testcase.class)
    public void toStringTests(Testcase testcase) {
        Assertions.assertEquals(testcase.pretty, testcase.actual.toString());
        Assertions.assertEquals(testcase.pretty, testcase.expected.toString());
    }

    enum InvalidRationalTestcase {
        POSITIV(() -> new Rational(1, 0)),
        NEGATIV(() -> new Rational(-1, 0)),
        ZERO(() -> new Rational(0, 0));

        private final Executable fn;

        InvalidRationalTestcase(Executable fn) {
            this.fn = fn;
        }
    }

    @ParameterizedTest
    @EnumSource(InvalidRationalTestcase.class)
    public void denominatorShouldNotBeZero(InvalidRationalTestcase testcase) throws Throwable {
        Assertions.assertThrows(InvalidNumber.class, testcase.fn);
    }

    enum MultiplicationTestcase {
        CASE_1(new Rational(1, 2), new Rational(1, 2), new Rational(1, 4)),
        CASE_2(new Rational(2, 1), new Rational(3, 1), new Rational(6, 1));

        public final Rational op1;
        public final Rational op2;
        public final Rational result;

        MultiplicationTestcase(Rational op1, Rational op2, Rational result) {
            this.op1 = op1;
            this.op2 = op2;
            this.result = result;
        }
    }

    @ParameterizedTest
    @EnumSource(MultiplicationTestcase.class)
    public void multiplyTests(MultiplicationTestcase testcase) {
        Assertions.assertEquals(testcase.result, testcase.op1.multiply(testcase.op2));
    }

    enum ParseTestcase {
        CASE_1("1/2", new Rational(1, 2)),
        CASE_2("1", new Rational(1, 1)),
        CASE_3("0", new Rational(0, 1)),
        CASE_4("-1", new Rational(-1, 1)),
        CASE_5("-1/2", new Rational(-1, 2));

        private final String input;
        private final Rational expected;

        ParseTestcase(String input, Rational expected) {

            this.input = input;
            this.expected = expected;
        }
    }

    @ParameterizedTest
    @EnumSource(ParseTestcase.class)
    public void parseTests(ParseTestcase testcase) {
        Assertions.assertEquals(testcase.expected, Rational.parse(testcase.input));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/", "1/", "/2", "something", "0.5", "0.7/0.8"})
    public void invalidStringsTests(String input) {
        Assertions.assertThrows(InvalidNumber.class, () -> Rational.parse(input));
    }
}
