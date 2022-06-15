package liquibase.util.grammar

import spock.lang.Specification
import spock.lang.Unroll

class SimpleSqlGrammarTest extends Specification {

    @Unroll
    def test() {
        when:
        def grammar = new SimpleSqlGrammar(new ByteArrayInputStream(input.getBytes()))

        def tokens = new ArrayList<String>()
        Token token
        while ((token = grammar.getNextToken()).kind != SimpleSqlGrammarConstants.EOF) {
            tokens.add(token.toString())
        }

        then:
        tokens == expected

        where:
        input                                  | expected
        ""                                     | []
        "sql goes here"                        | ["sql", " ", "goes", " ", "here"]
        "  odd    spacing  stuff  "            | ["  ", "odd", "    ", "spacing", "  ", "stuff", "  "]
        "create table test"                    | ["create", " ", "table", " ", "test"]
        "create table catalog.schema.test"     | ["create", " ", "table", " ", "catalog.schema.test"]
        "create table [test]"                  | ["create", " ", "table", " ", "[test]"]
        "create table \"test\""                | ["create", " ", "table", " ", "\"test\""]
        "create table /* comment here */ test" | ["create", " ", "table", " ", "/* comment here */", " ", "test"]
        "insert 'a string'"                    | ["insert", " ", "'a string'"]
        "invalid ' sql"                        | ["invalid", " ", "'", " ", "sql"]
        "utf8-〠＠chars works"                   | ["utf8", "-", "〠＠chars", " ", "works"]
    }
}
