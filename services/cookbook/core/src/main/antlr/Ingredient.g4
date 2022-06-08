grammar Ingredient;
@lexer::members {
    private boolean isValidUnit(String unit) {
        System.out.println("unit: " + unit);
        var result = family.haschka.wolkenschloss.cookbook.recipe.Unit.Companion.list().contains(unit);
        System.out.println("contains(" +  unit + ") = " + result);
        return result;
    }
}
line:
    quantity unit name EOF
    | quantity name EOF
    | name EOF
    ;

quantity:
    number rational
    | number
    | rational
;

mixed_fraction:
    sign? number rational EOF
    | sign? number EOF
    | sign? rational EOF
;

number: DIGIT;
rational : n=numerator '/' d=denominator | SYMBOL;
numerator  : DIGIT;
denominator : DIGIT;
sign : SIGN;
unit: UNIT;
name: NAME | name NAME;
WS          : [ \t\r\n]+ -> skip;

//ZERO    : '0';
//UINT    : [1-9] [0-9]*;
//INT     : '-'? UINT | ZERO;
SIGN    : '-';
DIGIT: [1-9] [0-9]*;
UNIT    : [\p{Alpha}\p{Punctuation} ]+ { isValidUnit(getText()) }?;
SYMBOL  : [½⅔¾⅘⅚⅞⅓⅗¼⅖⅝⅕⅙⅜⅐⅛⅑⅒];
//NAME    : [\p{Letter}\p{Mark}\p{Punctuation}\p{Symbol}][\p{Letter}\p{Mark}\p{Punctuation}\p{Symbol}]+;
NAME    : [\p{Letter}\p{Mark}\p{Punctuation}\p{Symbol}]+;