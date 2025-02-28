package liquibase.util

import liquibase.Scope
import liquibase.parser.LiquibaseSqlParser
import liquibase.parser.SqlParserFactory
import spock.lang.Specification
import spock.lang.Unroll

class SqlParserTest extends Specification {

    @Unroll("#featureName `#input`")
    def "parse with standard options"() {
        expect:
        SqlParserFactory sqlParserFactory = Scope.getCurrentScope().getSingleton(SqlParserFactory.class)
        LiquibaseSqlParser sqlParser = sqlParserFactory.getSqlParser()
        sqlParser.parse(input).toArray(true) == output

        where:
        input                                                                                              | output
        ""                                                                                                 | []
        "single"                                                                                           | ["single"]
        "a string"                                                                                         | ["a", "string"]
        "A CAP STRING"                                                                                     | ["A", "CAP", "STRING"]
        "a Mixed Case String"                                                                              | ["a", "Mixed", "Case", "String"]
        "using underscore_case"                                                                            | ["using", "underscore_case"]
        "here 'is a quoted string' and here is not"                                                        | ["here", "'is a quoted string'", "and", "here", "is", "not"]
        "'one quoted string' 'two quoted strings' 'three quoted strings'"                                  | ["'one quoted string'", "'two quoted strings'", "'three quoted strings'"]
        "Empty string ''"                                                                                  | ["Empty", "string", "''"]
        "'a string with '' and embedded quote' and more"                                                   | ["'a string with '' and embedded quote'", "and", "more"]
        "'''a string with '' and embedded quote '' and more ''' plus other"                                | ["'''a string with '' and embedded quote '' and more '''", "plus", "other"]
        "a string \"with\" \"double quotes\" \"\" in it"                                                   | ["a", "string", "\"with\"", "\"double quotes\"", "\"\"", "in", "it"]
        "  a    string  with various        length  spacing"                                               | ["a", "string", "with", "various", "length", "spacing"]
        "a\nstring\n\nwith\nnewlines"                                                                      | ["a", "string", "with", "newlines"]
        "a string /* with a comment */ here"                                                               | ["a", "string", "here"]
        "a string /* with a 'quoted' comment */ here"                                                      | ["a", "string", "here"]
        "a string /* with a \n'quoted' comment */ here"                                                    | ["a", "string", "here"]
        "a string /* with a \r\n'quoted' comment */ here"                                                  | ["a", "string", "here"]
        """a string /* with a
'quoted' comment */ here"""                                                                                | ["a", "string", "here"]
        "one string; and another"                                                                          | ["one", "string", ";", "and", "another"]
        "'a quoted semicolon; here' and /* a commented semicolon; here */"                                 | ["'a quoted semicolon; here'", "and"]
        "--here is a comment\nand a statement;\nand another --followed by a comment"                       | ["and", "a", "statement", ";", "and", "another"]
        "/*\nLets start a multiline comment\n\nThat actually covers multiple lines\nhere\n*/ then regular" | ["then", "regular"]
        "select 5 from test"                                                                               | ["select", "5", "from", "test"]
        "insert a1.b2.c3 from d4.e5"                                                                       | ["insert", "a1.b2.c3", "from", "d4.e5"]
        "select * from dual\ngo\ninsert into test\ngo"                                                     | ["select", "*", "from", "dual", "go", "insert", "into", "test", "go"]
        "create procedure test.name as select * from dual go"                                              | ["create", "procedure", "test.name", "as", "select", "*", "from", "dual", "go"]
        "create procedure \"test.name\" as select * from dual go"                                          | ["create", "procedure", "\"test.name\"", "as", "select", "*", "from", "dual", "go"]
        "create procedure \"test\".\"name\" as select * from dual go"                                      | ["create", "procedure", "\"test\".\"name\"", "as", "select", "*", "from", "dual", "go"]
        "create procedure [test].[name] as select * from dual go"                                          | ["create", "procedure", "[test].[name]", "as", "select", "*", "from", "dual", "go"]
        "a test.[name] b"                                                                                  | ["a", "test.[name]", "b"]
        "a [test].name b"                                                                                  | ["a", "[test].name", "b"]
        "a [test].\"name\".[here].[four] b"                                                                | ["a", "[test].\"name\".[here].[four]", "b"]
        "a + b"                                                                                            | ["a", "+", "b"]
        "a ~ b"                                                                                            | ["a", "~", "b"]
        "a > b"                                                                                            | ["a", ">", "b"]
        "a <> b"                                                                                           | ["a", "<", ">", "b"]
        "a != '\\\\' here"                                                                                      | ["a", "!", "=", "'\\\\'", "here"]

    }

    @Unroll
    def "parse with unicode"() {
        expect:
        SqlParserFactory sqlParserFactory = Scope.getCurrentScope().getSingleton(SqlParserFactory.class)
        LiquibaseSqlParser sqlParser = sqlParserFactory.getSqlParser()
        sqlParser.parse(input).toArray(true) == output

        where:
        input                                               | output
        "\u2002word regular\u2002unicode"                   | ["word", "regular", "unicode"]
        "x\u0282abc"                                        | ["x\u0282abc"] //unicode letter
        "x \u2013 abc"                                      | ["x", "\u2013", "abc"] //ndash synmbol
        "x 'quote with unicode punctuation \u2013 in it' y" | ["x", "'quote with unicode punctuation \u2013 in it'", "y"]
        "string with degree sign sample 32\u00b0C"          | ["string", "with", "degree", "sign", "sample", "32", "\u00b0", "C"]
        "currency symbols like € or ₿"                           | ["currency", "symbols",  "like", "€", "or", "₿"]
        "symbols ><#|\\!?£°§%&()[]{}^àìùòèéç"               | ["symbols",">","<", "#","|", "\\", "!", "?", "£","°", "§", "%", "&", "(",")","[","]","{","}","^","àìùòèéç"]
        "specials \$@"                                      | ["specials", "\$@"]
    }

    @Unroll("#featureName `#input`")
    def "parse with whitespace preserved"() {
        expect:
        SqlParserFactory sqlParserFactory = Scope.getCurrentScope().getSingleton(SqlParserFactory.class)
        LiquibaseSqlParser sqlParser = sqlParserFactory.getSqlParser()
        sqlParser.parse(input, true, false).toArray(true) == output

        where:
        input                                                                        | output
        ""                                                                           | []
        "single"                                                                     | ["single"]
        "a string"                                                                   | ["a", " ", "string"]
        "using underscore_case"                                                      | ["using", " ", "underscore_case"]
        "here 'is a quoted string' and here is not"                                  | ["here", " ", "'is a quoted string'", " ", "and", " ", "here", " ", "is", " ", "not"]
        "'one quoted string' 'two quoted strings' 'three quoted strings'"            | ["'one quoted string'", " ", "'two quoted strings'", " ", "'three quoted strings'"]
        "  a    string  with various        length  spacing   "                      | ["  ", "a", "    ", "string", "  ", "with", " ", "various", "        ", "length", "  ", "spacing", "   "]
        "\na\nstring\n\nwith\nnewlines\n\n"                                          | ["\n", "a", "\n", "string", "\n\n", "with", "\n", "newlines", "\n\n"]
        "a string /* with a comment */ here"                                         | ["a", " ", "string", " ", " ", "here"]
        "one string; and another"                                                    | ["one", " ", "string", ";", " ", "and", " ", "another"]
        "'a quoted semicolon; here' and /* a commented semicolon; here */"           | ["'a quoted semicolon; here'", " ", "and", " "]
        "--here is a comment\nand a statement;\nand another --followed by a comment" | ["\n", "and", " ", "a", " ", "statement", ";", "\n", "and", " ", "another", " "]
    }

    @Unroll("#featureName `#input`")
    def "parse with comments preserved"() {
        expect:
        SqlParserFactory sqlParserFactory = Scope.getCurrentScope().getSingleton(SqlParserFactory.class)
        LiquibaseSqlParser sqlParser = sqlParserFactory.getSqlParser()
        sqlParser.parse(input, false, true).toArray(true) == output

        where:
        input                                                                                              | output
        ""                                                                                                 | []
        "single"                                                                                           | ["single"]
        "a string"                                                                                         | ["a", "string"]
        "here 'is a quoted string' and here is not"                                                        | ["here", "'is a quoted string'", "and", "here", "is", "not"]
        "'one quoted string' 'two quoted strings' 'three quoted strings'"                                  | ["'one quoted string'", "'two quoted strings'", "'three quoted strings'"]
        "a\nstring\n\nwith\nnewlines"                                                                      | ["a", "string", "with", "newlines"]
        "a string /* with a comment */ here"                                                               | ["a", "string", "/* with a comment */", "here"]
        "a string /* with a 'quoted' comment */ here"                                                      | ["a", "string", "/* with a 'quoted' comment */", "here"]
        "'a quoted semicolon; here' and /* a commented semicolon; here */"                                 | ["'a quoted semicolon; here'", "and", "/* a commented semicolon; here */"]
        "--here is a comment\nand a statement;\nand another --followed by a comment"                       | ["--here is a comment\n", "and", "a", "statement", ";", "and", "another", "--followed by a comment\n"]
        "/*\nLets start a multiline comment\n\nThat actually covers multiple lines\nhere\n*/ then regular" | ["/*\nLets start a multiline comment\n\nThat actually covers multiple lines\nhere\n*/", "then", "regular"]
    }
}
