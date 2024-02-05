package liquibase.util.grammar

import spock.lang.Specification
import spock.lang.Unroll

class SimpleSqlGrammarTest extends Specification {

    @Unroll
    def test() {
        when:
        def tokenManager = new SimpleSqlGrammarTokenManager(new SimpleCharStream(new StringReader(input)));
        def grammar = new SimpleSqlGrammar(tokenManager)

        def tokens = new ArrayList<String>()
        Token token
        while ((token = grammar.getNextToken()).kind != SimpleSqlGrammarConstants.EOF) {
            tokens.add(token.toString())
        }

        then:
        tokens == expected

        where:
        input                                                  | expected
        ""                                                     | []
        "sql goes here"                                        | ["sql", " ", "goes", " ", "here"]
        "  odd    spacing  stuff  "                            | ["  ", "odd", "    ", "spacing", "  ", "stuff", "  "]
        "create table test"                                    | ["create", " ", "table", " ", "test"]
        "create table catalog.schema.test"                     | ["create", " ", "table", " ", "catalog.schema.test"]
        "create table [test]"                                  | ["create", " ", "table", " ", "[test]"]
        "create table \"test\""                                | ["create", " ", "table", " ", "\"test\""]
        "create table /* comment here */ test"                 | ["create", " ", "table", " ", "/* comment here */", " ", "test"]
        "insert 'a string'"                                    | ["insert", " ", "'a string'"]
        "escaped quotes ' '' '"                                | ["escaped", " ", "quotes", " ", "' '' '"]
        "escaped quotes ''''"                                  | ["escaped", " ", "quotes", " ", "''''"]
        "mysql escaped quotes ' \\' '"                         | ["mysql", " ", "escaped", " ", "quotes", " ", "' \\' '"]
        "mysql escaped quotes '\\''"                           | ["mysql", " ", "escaped", " ", "quotes", " ", "'\\''"]
        "invalid ' sql"                                        | ["invalid", " ", "'", " ", "sql"]
        "'invalid' ' sql"                                      | ["'invalid'", " ", "'", " ", "sql"]
        "utf8-〠＠chars works"                                   | ["utf8", "-", "〠＠chars", " ", "works"]
        "single '\\' works"                                    | ["single", " ", "'\\'", " ", "works"]
        "double '\\\\' works"                                  | ["double", " ", "'\\\\'", " ", "works"]
        "unquoted \\\\ works"                                  | ["unquoted", " ", "\\", "\\", " ", "works"]
        "'one quote' then 'two quote' then 'three quote' more" | ["'one quote'", " ", "then", " ", "'two quote'", " ", "then", " ", "'three quote'", " ", "more"]
        "'\\\\' then quotes '')"                               | ["'\\\\'", " ", "then", " ", "quotes", " ", "''", ")"]
        "stringwith escquote delim newline 'a\\'b;c\nd'"       | ["stringwith", " ", "escquote", " ", "delim", " ", "newline", " ", "'a\\'b;c\nd'"]
        "\"a\\\"b;c\nd\""                                      | ["\"a\\\"b;c\nd\""]
        "'''' sometimesEquals '\\''"                           | ["''''", " ", "sometimesEquals", " ", "'\\''"]
        "'\\'' sometimesEquals \"'\""                          | ["'\\''", " ", "sometimesEquals", " ", "\"'\""]
        "\"'\" sometimesEquals \"\\'\""                        | ["\"'\"", " ", "sometimesEquals", " ", "\"\\'\""]
        "This has a symbol ≤ (u2264) but no backslash"         | ["This", " ", "has", " ", "a", " ", "symbol", " ", "≤", " ", "(", "u2264", ")", " ", "but", " ", "no", " ", "backslash"]
        "This has a \\ and symbol ≤ (u2264)"                   | ["This", " ", "has", " ", "a", " ", "\\", " ", "and", " ", "symbol", " ", "≤", " ", "(", "u2264", ")"]
        "This ≤ (u2264) is before the \\"                      | ["This", " ", "≤", " ", "(", "u2264", ")", " ", "is", " ", "before", " ", "the", " ", "\\"]
        "This has an unicode char ÀÀÀÀÀÀ+++ãããioú≤₢"           | ["This", " ", "has", " ", "an", " ", "unicode"," ", "char", " ", "ÀÀÀÀÀÀ", "+", "+", "+", "ãããioú", "≤", "₢"]
    }
}
