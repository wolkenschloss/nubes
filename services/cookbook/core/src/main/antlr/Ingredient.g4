grammar Ingredient;
@lexer::members {
    private boolean isValidUnit(String unit) {
        var result = family.haschka.wolkenschloss.cookbook.recipe.Unit.Companion.list().contains(unit);
        return result;
    }
}

mixed_fraction:
    sign? rational EOF
    | sign? number rational EOF
    | sign? number EOF
    | ZERO EOF
;

line:
    quantity unit=UNIT name EOF
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
name: NAME
    | name NAME
    ;

WS      : [ \t\r\n]+ -> skip;
ZERO    : '0';
SIGN    : '-';
UINT    : [1-9][0-9]*;
UNIT    : [\p{Alpha}\p{Punctuation} ]+ { isValidUnit(getText()) }?;
SYMBOL  : [½⅔¾⅘⅚⅞⅓⅗¼⅖⅝⅕⅙⅜⅐⅛⅑⅒];
NAME    : [\p{Letter}\p{Mark}\p{Punctuation}\p{Symbol}]+;
