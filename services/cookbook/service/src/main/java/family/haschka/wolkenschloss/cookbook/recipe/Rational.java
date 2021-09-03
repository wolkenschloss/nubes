package family.haschka.wolkenschloss.cookbook.recipe;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collector;

public record Rational(int numerator, int denominator)  {

    public Rational {
        if (denominator == 0) {
            throw new InvalidNumber();
        }

        var gcd = gcd(numerator, denominator);
        numerator /= gcd;
        denominator /= gcd;
    }

    public Rational(int number, int numerator, int denominator) {
        this(numerator + number * denominator, denominator);
    }

    private static final Map<Rational, Character> unicodes;
    private static final Map<String, Rational> fractions;
    private static String fractionCharacterClass;
    public static final String REGEX;
    private static final Pattern pattern;

    static {
        unicodes = new HashMap<>();
        unicodes.put(new Rational(1, 2), '\u00bd');
        unicodes.put(new Rational(1, 3), '\u2153');
        unicodes.put(new Rational(2, 3), '\u2154');
        unicodes.put(new Rational(1, 4), '\u00bc');
        unicodes.put(new Rational(3, 4), '\u00be');
        unicodes.put(new Rational(1, 5), '\u2155');
        unicodes.put(new Rational(2, 5), '\u2156');
        unicodes.put(new Rational(3, 5), '\u2157');
        unicodes.put(new Rational(4, 5), '\u2158');
        unicodes.put(new Rational(1, 6), '\u2159');
        unicodes.put(new Rational(5, 6), '\u215a');
        unicodes.put(new Rational(1, 7), '\u2150');
        unicodes.put(new Rational(1, 8), '\u215b');
        unicodes.put(new Rational(3, 8), '\u215c');
        unicodes.put(new Rational(5, 8), '\u215d');
        unicodes.put(new Rational(7, 8), '\u215e');
        unicodes.put(new Rational(1, 9), '\u2151');
        unicodes.put(new Rational(1, 10), '\u2152');

        fractionCharacterClass = fractionCharacterClass();

        REGEX = "((?<number>-?(0|[1-9]\\d*)(?<!-0))[ ]?)?(((?<numerator>-?(0|[1-9]\\d*)(?<!-0))([\\/](?<denominator>[1-9]\\d*))*)|(?<fraction>[½⅔¾⅘⅚⅞⅓⅗¼⅖⅝⅕⅙⅜⅐⅛⅑⅒]))?";

//        REGEX = "((?<number>[-]?(0|[1-9]\\d*))\\s?)?(((?<numerator>[-]?[0-9]+)(/(?<denominator>[0-9]+))?)|"
//                + "(?<fraction>" + fractionCharacterClass + "))+";

//        REGEX = "((?<number>[-]?(0|[1-9]\\d*)))?((\\s(?<numerator>[-]?[0-9]+)(/(?<denominator>[0-9]+))?)|(\\s*"
//                + "(?<fraction>[½⅔¾⅘⅚⅞⅓⅗¼⅖⅝⅕⅙⅜⅐⅛⅑⅒])))?";

        pattern = Pattern.compile("^" + REGEX + "$");

        fractions = new HashMap<>();
        for (Rational r : unicodes.keySet()) {
            fractions.put(unicodes.get(r).toString(), r);
        }
    }

    public Rational(int value) {
        this(value, 1);
    }

    private int gcd(int a, int b) {
        if (b == 0) return a;
        return gcd(b, a % b);
    }

    public Rational multiply(Rational b) {
        return new Rational(numerator * b.numerator, denominator * b.denominator);
    }

    public static String fractionCharacterClass() {
        var result = new StringBuilder();
        result.append('[');
        var allFractions = unicodes.values().stream().collect(Collector.of(
                StringBuilder::new,
                StringBuilder::append,
                StringBuilder::append,
                StringBuilder::toString));
        result.append(allFractions);
        result.append(']');
        return result.toString();
    }

    public static Rational parse(String input) {

        Matcher m = pattern.matcher(input);
        if (m.find()) {
            String number = m.group("number");
            String fraction = m.group("fraction");
            String numerator = m.group("numerator");
            String denominator = m.group("denominator");

            if (number != null) {
                if (fraction != null) {
                    return fractions.get(fraction)
                            .add(new Rational(Integer.parseInt(number), 1));
                } else if (numerator != null) {
                    return new Rational(
                            Integer.parseInt(number),
                            Integer.parseInt(numerator),
                            Integer.parseInt(denominator));
                } else {
                    return new Rational(Integer.parseInt(number));
                }
            } else {
                if (fraction != null) {
                    return fractions.get(fraction);
                } else if (numerator != null) {
                    return new Rational(Integer.parseInt(numerator), Integer.parseInt(denominator));
                }
            }
        }

        throw new InvalidNumber();
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();

        if (denominator == 1) {
            builder.append(numerator);
        } else {
            var number = numerator / denominator;

            if (number != 0) {
                builder.append(number);
                builder.append(" ");

                var rest = this.minus(new Rational(number, 1));
                appendRational(builder, rest);
            } else {
                appendRational(builder, this);
            }
        }

        return builder.toString();
    }

    private void appendRational(StringBuilder builder, Rational rest) {
        if (unicodes.containsKey(rest)) {
            builder.append(unicodes.get(rest));
        } else {
            builder.append(rest.numerator);
            builder.append("/");
            builder.append(rest.denominator);
        }
    }

    public Rational minus(Rational rational) {
        var d = denominator * rational.denominator;
        var a = numerator * rational.denominator;
        var b = rational.numerator * denominator;
        return new Rational(a - b, d);
    }

    public Rational add(Rational rational) {
        var d = denominator * rational.denominator;
        var a = numerator * rational.denominator;
        var b = rational.numerator * denominator;

        return new Rational(a + b, d);
    }
}
