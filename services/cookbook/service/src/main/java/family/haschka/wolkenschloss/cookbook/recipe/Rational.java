package family.haschka.wolkenschloss.cookbook.recipe;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private static final Map<Rational, Character> codes;
    private static final Map<String, Rational> fractions;
    public static final String REGEX;
    private static final Pattern pattern;

    static {
        codes = new HashMap<>();
        codes.put(new Rational(1, 2), '\u00bd');
        codes.put(new Rational(1, 3), '\u2153');
        codes.put(new Rational(2, 3), '\u2154');
        codes.put(new Rational(1, 4), '\u00bc');
        codes.put(new Rational(3, 4), '\u00be');
        codes.put(new Rational(1, 5), '\u2155');
        codes.put(new Rational(2, 5), '\u2156');
        codes.put(new Rational(3, 5), '\u2157');
        codes.put(new Rational(4, 5), '\u2158');
        codes.put(new Rational(1, 6), '\u2159');
        codes.put(new Rational(5, 6), '\u215a');
        codes.put(new Rational(1, 7), '\u2150');
        codes.put(new Rational(1, 8), '\u215b');
        codes.put(new Rational(3, 8), '\u215c');
        codes.put(new Rational(5, 8), '\u215d');
        codes.put(new Rational(7, 8), '\u215e');
        codes.put(new Rational(1, 9), '\u2151');
        codes.put(new Rational(1, 10), '\u2152');

        REGEX = "((?<number>-?(0|[1-9]\\d*)(?<!-0))[ ]?)?(((?<numerator>-?(0|[1-9]\\d*)(?<!-0))([/](?<denominator>[1-9]\\d*))*)|(?<fraction>[½⅔¾⅘⅚⅞⅓⅗¼⅖⅝⅕⅙⅜⅐⅛⅑⅒]))?";
        pattern = Pattern.compile("^" + REGEX + "$");

        fractions = new HashMap<>();
        for (Rational r : codes.keySet()) {
            fractions.put(codes.get(r).toString(), r);
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
        if (codes.containsKey(rest)) {
            builder.append(codes.get(rest));
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
