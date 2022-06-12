parser grammar IngredientParser;

options { tokenVocab = IngredientLexer; }

line:
    quantity unit=UNIT SEPARATOR name EOF
    | quantity name EOF
    | name EOF
    ;

mixed_fraction:
    sign? rational EOF
    | sign? number rational EOF
    | sign? number EOF
    | ZERO EOF
;


quantity:
    rational
    | number rational
    | number
;

number: UINT;
rational : numerator DIV denominator | SYMBOL;
numerator  : UINT;
denominator : UINT;
sign : SIGN;
name: TEXT_START? REST?;
