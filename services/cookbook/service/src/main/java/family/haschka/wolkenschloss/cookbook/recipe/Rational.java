package family.haschka.wolkenschloss.cookbook.recipe;

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
        Pattern p = Pattern.compile("^(?<numerator>[-]?[0-9]+)(/(?<denominator>[0-9]+))?$");
        Matcher m = p.matcher(input);
        if(m.find()) {
            String numerator = m.group("numerator");
            String denominator = m.group("denominator");

            if (denominator != null) {
                return new Rational(Integer.parseInt(numerator), Integer.parseInt(denominator));
            }

            return new Rational(Integer.parseInt(numerator), 1);
        }

        throw new InvalidNumber();
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(numerator);
        if (denominator != 1) {
            builder.append("/");
            builder.append(denominator);
        }
        return builder.toString();
    }
}
