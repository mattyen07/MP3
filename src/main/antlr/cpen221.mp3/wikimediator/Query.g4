grammar Query;

// This puts "package cpen221.mp3;" for all generated Java files.
@header {
package cpen221.mp3.wikimediator;
}

// This adds code to the generated lexer and parser.
// DO NOT CHANGE THESE LINES
@members {
    // This method makes the lexer or parser stop running if it encounters
    // invalid input and throw a RuntimeException.
    public void reportErrorsAsExceptions() {
        //removeErrorListeners();
        addErrorListener(new ExceptionThrowingErrorListener());
    }

    private static class ExceptionThrowingErrorListener
                                              extends BaseErrorListener {
        @Override
        public void syntaxError(Recognizer<?, ?> recognizer,
                Object offendingSymbol, int line, int charPositionInLine,
                String msg, RecognitionException e) {

            throw new RuntimeException(msg);
        }
    }
}


/*
 * These are the lexical rules. They define the tokens used by the lexer.
 */
GET : 'get' ;
WHERE : 'where' ;
LPAREN : '(' ;
RPAREN : ')' ;
AND : 'and' ;
OR : 'or' ;
TITLE : 'title is' ;
AUTHOR: 'author is' ;
CATEGORY: 'category is' ;
ITEM : 'page' | 'author' | 'category' ;
SORTED : 'asc' | 'desc';
STRING : '\'' ( ~'\'' | '\'\'' )* '\'' ;
WHITESPACE : [ \t\r\n]+ -> skip ;
/*
 * These are the parser rules. They define the structures used by the parser.
 * Antlr requires grammar nonterminals to be lowercase.
 */
query : GET ITEM WHERE condition SORTED? EOF ;
condition : simpleCondition | LPAREN condition AND condition RPAREN | LPAREN condition OR condition RPAREN ;
simpleCondition : TITLE STRING | AUTHOR STRING | CATEGORY STRING ;

