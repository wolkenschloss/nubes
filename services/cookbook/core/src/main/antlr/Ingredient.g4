grammar Ingredient;
@lexer::members {
    private boolean isValidUnit(String unit) {
        System.out.println("unit: " + unit);
        var result = family.haschka.wolkenschloss.cookbook.recipe.Unit.Companion.list().contains(unit);
        System.out.println("contains(" +  unit + ") = " + result);
        return result;
    }
}

mixed_fraction:
//ZERO | number | rational | number rational;
    sign? rational EOF
    | sign? number rational EOF
    | sign? number EOF
    | ZERO EOF
;

line:
    quantity unit name EOF
    | quantity name EOF
    | name EOF
    ;

quantity:
    rational
    | number rational
    | number
;



number: UINT;
rational : numerator '/' denominator | SYMBOL;
numerator  : UINT;
denominator : UINT;
sign : SIGN;
unit: UNIT;
name: NAME | name NAME;
WS          : [ \t\r\n]+ -> skip;

ZERO    : '0';
//UINT    : [1-9] [0-9]*;
//INT     : '-'? UINT | ZERO;
SIGN    : '-';
//INT     : '-'? [1-9][0-9]*;
UINT    : [1-9][0-9]*;
//UINT: [0-9]+;
UNIT    : [\p{Alpha}\p{Punctuation} ]+ { isValidUnit(getText()) }?;
SYMBOL  : [½⅔¾⅘⅚⅞⅓⅗¼⅖⅝⅕⅙⅜⅐⅛⅑⅒];
//NAME    : [\p{Letter}\p{Mark}\p{Punctuation}\p{Symbol}][\p{Letter}\p{Mark}\p{Punctuation}\p{Symbol}]+;
NAME    : [\p{Letter}\p{Mark}\p{Punctuation}\p{Symbol}]+;