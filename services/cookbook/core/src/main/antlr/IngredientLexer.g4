lexer grammar IngredientLexer;

@header {
    import family.haschka.wolkenschloss.cookbook.recipe.Unit;
}

@lexer::members {
    private boolean isValidUnit(String unit) {
        return Unit.Companion.list().contains(unit);
    }
}

WS      : [ \t\r\n]+ -> skip;
DIV     : '/';
ZERO    : '0';
SIGN    : '-';
UINT    : [1-9][0-9]*;
UNIT    : [\p{Alpha}\p{Punctuation} ]+ { isValidUnit(getText()) }? -> pushMode(TEXT_SEPERATOR);
SYMBOL  : [½⅔¾⅘⅚⅞⅓⅗¼⅖⅝⅕⅙⅜⅐⅛⅑⅒];
TEXT_START: [\p{Letter}\p{Mark}\p{Punctuation}\p{Symbol}] -> pushMode(TEXT) ;

mode TEXT_SEPERATOR;
SEPARATOR: ' '+ -> pushMode(TEXT);

mode TEXT;
REST    : [\p{Alnum}\p{Mark}\p{Punctuation}\p{Symbol} ]+;
END     : EOF -> popMode;
