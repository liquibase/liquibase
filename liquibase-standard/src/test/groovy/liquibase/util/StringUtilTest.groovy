package liquibase.util

import liquibase.change.core.CreateTableChange
import org.hamcrest.Matchers
import spock.lang.Specification
import spock.lang.Unroll

import static spock.util.matcher.HamcrestSupport.that

class StringUtilTest extends Specification {

    @Unroll
    def "processMultilineSql examples"() {
        expect:
        that Arrays.asList(StringUtil.processMultiLineSQL(rawString, stripComments, splitStatements, endDelimiter)), Matchers.contains(expected.toArray())

        where:
        stripComments | splitStatements | endDelimiter | rawString                                                                                                                                                                                           | expected
        true          | true            | null         | "/**\nSome comments go here\n**/\ncreate table sqlfilerollback (id int);\n\n/**\nSome morecomments go here\n**/\ncreate table sqlfilerollback2 (id int);"                                           | ["create table sqlfilerollback (id int)", "create table sqlfilerollback2 (id int)"]
        true          | true            | null         | "/*\nThis is a test comment of MS-SQL script\n*/\n\nSelect * from Test;\nUpdate Test set field = 1"                                                                                                 | ["Select * from Test", "Update Test set field = 1"]
        true          | true            | null         | "some sql/*Some text\nmore text*/more sql"                                                                                                                                                          | ["some sqlmore sql"]
        true          | true            | null         | "insert into test_table values(1, 'hello');\ninsert into test_table values(2, 'hello');\n--insert into test_table values(3, 'hello');\ninsert into test_table values(4, 'hello');"                  | ["insert into test_table values(1, 'hello')", "insert into test_table values(2, 'hello')", "insert into test_table values(4, 'hello')"]
        false         | true            | null         | "some sql/*Some text\nmore text*/more sql"                                                                                                                                                          | ["some sql/*Some text\nmore text*/more sql"]
        true          | true            | null         | "/*\nThis is a test comment of SQL script\n*/\n\nSelect * from Test;\nUpdate Test set field = 1"                                                                                                    | ["Select * from Test", "Update Test set field = 1"]
        true          | true            | null         | "select * from simple_select_statement;\ninsert into table ( col ) values (' value with; semicolon ');"                                                                                             | ["select * from simple_select_statement", "insert into table ( col ) values (' value with; semicolon ')"]
        true          | true            | null         | "--\n-- Create the blog table.\n--\nCREATE TABLE blog\n(\n ID NUMBER(15) NOT NULL\n)"                                                                                                               | ["CREATE TABLE blog\n(\n ID NUMBER(15) NOT NULL\n)"]
        true          | true            | null          | "statement 1/2\n/\nstatement 2/2"                                                                                                                                                                   | ["statement 1/2", "statement 2/2"]
        true          | true            | "//"         | "drop procedure if exists my_proc//\n\ncreate procedure my_proc(i_myvar varchar)\nbegin\n  a bunch of code here\nend//"                                                                             | ["drop procedure if exists my_proc", "create procedure my_proc(i_myvar varchar)\nbegin\n  a bunch of code here\nend"]
        true          | true            | "/"          | "CREATE OR REPLACE PACKAGE emp_actions AS  -- spec\nTYPE EmpRecTyp IS RECORD (emp_id INT, salary REAL);\nCURSOR desc_salary RETURN EmpRecTyp);\nEND emp_actions;"                                   | ["CREATE OR REPLACE PACKAGE emp_actions AS  \nTYPE EmpRecTyp IS RECORD (emp_id INT, salary REAL);\nCURSOR desc_salary RETURN EmpRecTyp);\nEND emp_actions;"]
        true          | true            | "/"          | "CREATE OR REPLACE PACKAGE emp_actions AS  -- spec\nTYPE EmpRecTyp IS RECORD (emp_id INT, salary REAL);\nCURSOR desc_salary RETURN EmpRecTyp);\nEND emp_actions;\n/\nanother statement;here\n/\n"   | ["CREATE OR REPLACE PACKAGE emp_actions AS  \nTYPE EmpRecTyp IS RECORD (emp_id INT, salary REAL);\nCURSOR desc_salary RETURN EmpRecTyp);\nEND emp_actions;", "another statement;here"]
        true          | true            | "/"          | "statement 1/2\n/\nstatement 2/2"                                                                                                                                                                   | ["statement 1/2", "statement 2/2"]
        true          | true            | "/"          | "/* a comment here */ statement 1/2\n/\nstatement 2/2"                                                                                                                                              | ["statement 1/2", "statement 2/2"]
        false         | true            | "/"          | "/* a comment here */ statement 1/2\n/\nstatement 2/2"                                                                                                                                              | ["/* a comment here */ statement 1/2", "statement 2/2"]
        true          | true            | "/"          | "/\nstatement here"                                                                                                                                                                                 | ["statement here"]
        true          | true            | "\\n/"       | "CREATE OR REPLACE PACKAGE emp_actions AS  -- spec\nTYPE EmpRecTyp IS RECORD (emp_id INT, salary REAL);\nCURSOR desc_salary RETURN EmpRecTyp);\nEND emp_actions;\n/\nanother statement;here\n/\n"   | ["CREATE OR REPLACE PACKAGE emp_actions AS  \nTYPE EmpRecTyp IS RECORD (emp_id INT, salary REAL);\nCURSOR desc_salary RETURN EmpRecTyp);\nEND emp_actions;", "another statement;here"]
        true          | true            | "\\ngo"      | "CREATE OR REPLACE PACKAGE emp_actions AS  -- spec\nTYPE EmpRecTyp IS RECORD (emp_id INT, salary REAL);\nCURSOR desc_salary RETURN EmpRecTyp);\nEND emp_actions;\nGO\nanother statement;here\nGO\n" | ["CREATE OR REPLACE PACKAGE emp_actions AS  \nTYPE EmpRecTyp IS RECORD (emp_id INT, salary REAL);\nCURSOR desc_salary RETURN EmpRecTyp);\nEND emp_actions;", "another statement;here"]
        true          | true            | null         | "statement 1;\nstatement 2;\nGO\n\nstatement 3; statement 4;"                                                                                                                                       | ["statement 1", "statement 2", "statement 3", "statement 4"]
        true          | true            | "\\nGO"      | "statement 1 \nGO\nstatement 2"                                                                                                                                                                     | ["statement 1", "statement 2"]
        true          | true            | "\\nGO"      | "CREATE OR REPLACE PACKAGE emp_actions AS  -- spec\nTYPE EmpRecTyp IS RECORD (emp_id INT, salary REAL);\nCURSOR desc_salary RETURN EmpRecTyp);\nEND emp_actions;\nGO\nanother statement;here\nGO\n" | ["CREATE OR REPLACE PACKAGE emp_actions AS  \nTYPE EmpRecTyp IS RECORD (emp_id INT, salary REAL);\nCURSOR desc_salary RETURN EmpRecTyp);\nEND emp_actions;", "another statement;here"]
        true          | true            | null         | "CREATE OR REPLACE PACKAGE emp_actions AS BEGIN\n statement 1;\nanother statement;here; END;"                                                                                                       | ["CREATE OR REPLACE PACKAGE emp_actions AS BEGIN\n statement 1;\nanother statement;here; END"]
        true          | true            | null         | "CREATE OR REPLACE PACKAGE emp_actions AS BEGIN\n statement 1;\nBEGIN a nested statement;here; END; END;"                                                                                           | ["CREATE OR REPLACE PACKAGE emp_actions AS BEGIN\n statement 1;\nBEGIN a nested statement;here; END; END"]
        true          | true            | null         | "BEGIN TRANSACTION; statement 1; end transaction;"                                                                                                                                                  | ["BEGIN TRANSACTION", "statement 1", "end transaction"]

    }

    @Unroll
    def "stripComments examples"() {
        expect:
        StringUtil.stripComments(rawString) == expected

        where:
        rawString                                                 | expected
        " Some text but no comments"                              | "Some text but no comments"
        "Some text -- with comment"                               | "Some text"
        "Some text -- with comment\n"                             | "Some text"
        "Some text--with comment\nMore text--and another comment" | "Some text\nMore text"
        "/*Some text\nmore text*/"                                | ""
        "some sql/*Some text\nmore text*/"                        | "some sql"
        "/*Some text\nmore text*/some sql"                        | "some sql"
        "some sql/*Some text\nmore text*/more sql"                | "some sqlmore sql"
    }

    @Unroll
    def "splitSql examples"() {
        expect:
        that Arrays.asList(StringUtil.splitSQL(rawString, endDelimiter)), Matchers.contains(expected.toArray())

        where:
        endDelimiter | rawString                                                                                                                                                                                                                                                                                                                                     | expected
        null         | "some sql\ngo\nmore sql"                                                                                                                                                                                                                                                                                                                      | ["some sql", "more sql"]
        null         | "some sql\nGO\nmore sql"                                                                                                                                                                                                                                                                                                                      | ["some sql", "more sql"]
        null         | "SELECT *\n FROM sys.objects\n WHERE object_id = OBJECT_ID(N'[test].[currval]')\n AND type in (N'FN', N'IF', N'TF', N'FS', N'FT')\n )\n DROP FUNCTION [test].[currval]\ngo\nIF EXISTS\n(\n SELECT *\n FROM sys.objects\n WHERE object_id = OBJECT_ID(N'[test].[nextval]')\n AND type in (N'P', N'PC')\n )\n DROP PROCEDURE [test].[nextval]:" | ["SELECT *\n FROM sys.objects\n WHERE object_id = OBJECT_ID(N'[test].[currval]')\n AND type in (N'FN', N'IF', N'TF', N'FS', N'FT')\n )\n DROP FUNCTION [test].[currval]", "IF EXISTS\n(\n SELECT *\n FROM sys.objects\n WHERE object_id = OBJECT_ID(N'[test].[nextval]')\n AND type in (N'P', N'PC')\n )\n DROP PROCEDURE [test].[nextval]:"]
        "X"          | "insert into datatable (col) values ('a value with a ;') X\ninsert into datatable (col) values ('another value with a ;') X"                                                                                                                                                                                                                  | ["insert into datatable (col) values ('a value with a ;')", "insert into datatable (col) values ('another value with a ;')"]
    }

    def "stripComments performance is reasonable with a long string"() {
        when:
        int maxAttempts = 10
        int acceptableMs = 800
        int attemptsLeft
        String sql
        String result
        long start
        long end

        for (attemptsLeft = maxAttempts; attemptsLeft > 0; attemptsLeft--) {
            StringBuilder sqlBuilder = new StringBuilder()
            for (int i = 0; i < 10000; ++i) {
                sqlBuilder.append(" A")
            }
            sql = sqlBuilder.toString()
            String comment = " -- with comment\n"
            String totalLine = sql + comment
            start = System.currentTimeMillis()
            result = StringUtil.stripComments(totalLine)
            end = System.currentTimeMillis()
            if ((end - start) <= acceptableMs) {
                break
            }
        }

        then:
        result == sql.trim()
        assert end - start <= acceptableMs: "Did not complete within " + acceptableMs +
                "ms, took " + (end - start) + "ms"
    }

    def "join with map"() {
        expect:
        StringUtil.join((Map) map as Map, delimiter) == value

        where:
        map                               | value                    | delimiter
        new HashMap()                     | ""                       | ", "
        [key1: "a"]                       | "key1=a"                 | ", "
        [key1: "a", key2: "b"]            | "key1=a, key2=b"         | ", "
        [key1: "a", key2: "b"]            | "key1=aXkey2=b"          | "X"
        [key1: "a", key2: "b", key3: "c"] | "key1=a, key2=b, key3=c" | ", "
    }

    def "to lower without whitespaces"() {
        expect:
        StringUtil.toLowerWithoutWhitespaces(string) == expected

        where:
        string                | expected
        "First Value"         | "firstvalue"
        "secondValue"         | "secondvalue"
        "   Third Value   \n" | "thirdvalue"
        null                  | null
    }

    @Unroll
    def "join sorted"() {
        expect:
        StringUtil.join(array, ",", sorted) == expected

        where:
        array           | sorted | expected
        ["a", "c", "b"] | true   | "a,b,c"
        ["a", "c", "b"] | false  | "a,c,b"
    }

    @Unroll
    def "join with formatter sorted"() {
        expect:
        StringUtil.join(array, ",", new StringUtil.ToStringFormatter(), sorted) == expected

        where:
        array     | sorted | expected
        [1, 3, 2] | true   | "1,2,3"
        [1, 3, 2] | false  | "1,3,2"
    }


    @Unroll
    def "pad"() {
        expect:
        StringUtil.pad(input, pad) == output

        where:
        input   | pad | output
        null    | 0   | ""
        null    | 3   | "   "
        ""      | 0   | ""
        ""      | 3   | "   "
        " "     | 3   | "   "
        "abc"   | 2   | "abc"
        "abc"   | 3   | "abc"
        "abc  " | 3   | "abc"
        "abc"   | 5   | "abc  "
        "abc "  | 5   | "abc  "
    }

    @Unroll
    def "leftPad"() {
        expect:
        StringUtil.leftPad(input, pad) == output

        where:
        input   | pad | output
        null    | 0   | ""
        null    | 3   | "   "
        ""      | 0   | ""
        ""      | 3   | "   "
        " "     | 3   | "   "
        "abc"   | 2   | "abc"
        "abc"   | 3   | "abc"
        "abc  " | 3   | "abc"
        "abc"   | 5   | "  abc"
        "abc "  | 5   | "  abc"
        " abc"  | 5   | "  abc"
    }

    @Unroll
    def "stripSqlCommentsFromTheEnd"() {
        expect:
        StringUtil.stripSqlCommentsAndWhitespacesFromTheEnd(input) == output

        where:
        input                                                                                                                                            | output
        null                                                                                                                                             | null
        ""                                                                                                                                               | ""
        "   "                                                                                                                                            | ""
        "no comments"                                                                                                                                    | "no comments"
        "-- some comments"                                                                                                                               | ""
        "   -- some comments   "                                                                                                                         | ""
        "create table mtable; --some line comments"                                                                                                      | "create table mtable;"
        "some txt; \n-- line cmt \n--another "                                                                                                           | "some txt;"
        "some txt; \n-- line cmt \n--another \n /* and block\\/ */"                                                                                      | "some txt;"
        "some txt; \n-- line cmt \n--another \n /* and block\\/ */\\t\n\ndefine something;--last comment"                                                | "some txt; \n-- line cmt \n--another \n /* and block\\/ */\\t\n\ndefine something;"
        "some txt; \n-- line cmt \n--another \n /* and block\\/ */\\t\n\ndefine something;--last comment\n/*****another\nblock\n***/"                    | "some txt; \n-- line cmt \n--another \n /* and block\\/ */\\t\n\ndefine something;"
        "some txt; \n-- line cmt \n--another \n /* and block\\/ */\\t\n\ndefine something;\t--last comment\n\n\n\t--another\n/*****another\nblock\n***/" | "some txt; \n-- line cmt \n--another \n /* and block\\/ */\\t\n\ndefine something;"

    }


    @Unroll
    def "concatConsistentCase"() {
        expect:
        StringUtil.concatConsistentCase(base, addition) == expected

        where:
        base  | addition | expected
        "abc" | "_xyz"   | "abc_xyz"
        "abc" | "_XYZ"   | "abc_xyz"
        "abc" | "_XyZ"   | "abc_xyz"

        "ABC" | "_xyz"   | "ABC_XYZ"
        "ABC" | "_XYZ"   | "ABC_XYZ"
        "ABC" | "_XyZ"   | "ABC_XYZ"

        "AbC" | "_xyz"   | "AbC_xyz"
        "AbC" | "_XYZ"   | "AbC_XYZ"
        "AbC" | "_XyZ"   | "AbC_XyZ"

        "a1"  | "_x"     | "a1_x"
        "A1"  | "_x"     | "A1_X"
        "123" | "_x"     | "123_x"
        "123" | "_X"     | "123_X"
    }

    @Unroll("#featureName: '#input'")
    def "trimToNull"() {
        expect:
        StringUtil.trimToNull(input) == expected

        where:
        input                     | expected
        "test string"             | "test string"
        "test string   "          | "test string"
        "   test string"          | "test string"
        "   test string     "     | "test string"
        "test    string"          | "test    string"
        ""                        | null
        "    "                    | null
        "\n\r\ttest string\r\n\t" | "test string"
    }

    @Unroll
    def "toKabobCase"() {
        expect:
        StringUtil.toKabobCase(input) == expected

        where:
        input           | expected
        "a"             | "a"
        "testValue"     | "test-value"
        "testValueHere" | "test-value-here"
        "TestValueHere" | "test-value-here"
        "sQL"           | "s-q-l"
        null            | null
    }

    @Unroll
    def "toCamelCase"() {
        expect:
        StringUtil.toCamelCase(input) == expected

        where:
        input             | expected
        "a"               | "a"
        "test-value"      | "testValue"
        "test-Value"      | "testValue"
        "test_value"      | "testValue"
        "test-value_here" | "testValueHere"
        "testValue"       | "testValue"
        "testValue"       | "testValue"
        "s-q-l"           | "sQL"
        null              | null
    }

    @Unroll
    def join() {
        expect:
        StringUtil.join(input, delimiter) == expected

        where:
        input                   | delimiter | expected
        []                      | "x"       | ""
        ["a", "b"] as List      | ","       | "a,b"
        ["a", "b", "c"] as List | " X "     | "a X b X c"
        ["a", "b"] as String[]  | ","       | "a,b"
        [1, 2] as Integer[]     | ","       | "1,2"
        [1, 2] as int[]         | ","       | "1,2"
        ["a", "b"] as TreeSet   | ","       | "a,b"
        ["a": 1, "b": 2] as Map | ","       | "a=1,b=2"
    }

    @Unroll
    def "join with complex options"() {
        when:
        def extensible = new CreateTableChange()
        extensible.set("schemaName", "schema_name")
        extensible.set("tableName", "table_name")

        then:
        StringUtil.join(["a", null, "b"], "/", new StringUtil.StringUtilFormatter() {
            @Override
            String toString(Object obj) {
                return "X" + obj + "X"
            }
        }) == "XaX/XnullX/XbX"

        StringUtil.join(["c", "a", null, "b"], "/", new StringUtil.StringUtilFormatter() {
            @Override
            String toString(Object obj) {
                return "X" + obj + "X"
            }
        }, true) == "XaX/XbX/XcX/XnullX"

        StringUtil.join(extensible, ", ") == "schemaName=schema_name, tableName=table_name"
    }

    @Unroll
    def splitAndTrim() {
        expect:
        StringUtil.splitAndTrim(input, regexp) == expected

        where:
        input               | regexp | expected
        " a  , value, here" | ","    | ["a", "value", "here"]
        null                | ","    | null
        ""                  | "X"    | [""]
    }

    def repeat() {
        expect:
        StringUtil.repeat("xa", 4) == "xaxaxaxa";
        StringUtil.repeat("x", 0) == "";
    }

    def indent() {
        expect:
        StringUtil.indent("a string") == "    a string"
        StringUtil.indent("a string", 6) == "      a string"
        StringUtil.indent("a string", 0) == "a string"
        StringUtil.indent(null, 3) == null
    }

    def "uppercaseFirst"() {
        expect:
        StringUtil.upperCaseFirst(null) == null
        StringUtil.upperCaseFirst("") == ""
        StringUtil.upperCaseFirst("a") == "A"
        StringUtil.upperCaseFirst("abc") == "Abc"
    }

    def "lowercaseFirst"() {
        expect:
        StringUtil.lowerCaseFirst(null) == null
        StringUtil.lowerCaseFirst("") == ""
        StringUtil.lowerCaseFirst("A") == "a"
        StringUtil.lowerCaseFirst("ABC") == "aBC"
    }


    def "hasLowerCase"() {
        expect:
        !StringUtil.hasLowerCase(null)
        !StringUtil.hasLowerCase("")
        !StringUtil.hasLowerCase("ABC")
        StringUtil.hasLowerCase("aBc")
        StringUtil.hasLowerCase("abC")
        !StringUtil.hasLowerCase("ABC")
    }

    def "hasUpperCase"() {
        expect:
        !StringUtil.hasUpperCase(null)
        !StringUtil.hasUpperCase("")
        StringUtil.hasUpperCase("ABC")
        StringUtil.hasUpperCase("AbC")
        StringUtil.hasUpperCase("aBC")
        !StringUtil.hasUpperCase("abc")
    }

    def "standardizeLineEndings"() {
        expect:
        StringUtil.standardizeLineEndings(null) == null
        StringUtil.standardizeLineEndings("") == ""
        StringUtil.standardizeLineEndings("a\r\nb\r\n") == "a\nb\n"
        StringUtil.standardizeLineEndings("a\rb\r") == "a\nb\n"
        StringUtil.standardizeLineEndings("a\nb\n") == "a\nb\n"
    }

    @Unroll
    def "isAscii"() {
        expect:
        StringUtil.isAscii(input) == expected

        where:
        input | expected
        null  | true
        ""    | true
        "a"   | true
        "abc" | true
        "ab¢" | false
    }

    @Unroll
    def "escapeHtml"() {
        expect:
        StringUtil.escapeHtml(input) == expected

        where:
        input | expected
        null  | null
        ""    | ""
        "abc" | "abc"
        "ab¢" | "ab&#162;"
    }

    @Unroll
    def "isEmpty and isNotEmpty"() {
        expect:
        StringUtil.isEmpty(input) == empty
        StringUtil.isNotEmpty(input) == !empty

        where:
        input | empty
        null  | true
        ""    | true
        "a"   | false
        "abc" | false
    }

    @Unroll
    def "startsWith"() {
        expect:
        StringUtil.startsWith(value, startsWith) == expected

        where:
        value | startsWith | expected
        null  | "x"        | false
        ""    | "x"        | false
        "a"   | "x"        | false
        "ax"  | "x"        | false
        "abc" | "ab"       | true
    }

    @Unroll
    def "endsWith"() {
        expect:
        StringUtil.endsWith(value, endsWith) == expected

        where:
        value | endsWith | expected
        null  | "x"        | false
        ""    | "x"        | false
        "a"   | "x"        | false
        "ax"  | "a"        | false
        "abc" | "bc"       | true
    }

    @Unroll
    def "contains"() {
        expect:
        StringUtil.contains(value, contains) == expected

        where:
        value | contains   | expected
        null  | "x"        | false
        ""    | "x"        | false
        "a"   | "x"        | false
        "ax"  | "a"        | true
        "abc" | "bc"       | true
    }

    @Unroll
    def "isWhitespace"() {
        expect:
        StringUtil.isWhitespace(value) == expected

        where:
        value | expected
        null  | true
        ""    | true
        "a"   | false
        "a x" | false
        "   " | true
    }

    @Unroll
    def "isMinimumVersion"() {
        expect:
        StringUtil.isMinimumVersion(version, major, minor, patch) == expected

        where:
        version  | major | minor | patch | expected
        null     | 1     | 1     | 1     | true
        "10"     | 1     | 2     | 3     | false
        "10"     | 10    | 0     | 0     | true
        "10"     | 10    | 5     | 1     | true
        "10.2"   | 10    | 0     | 1     | false
        "10.2"   | 10    | 5     | 3     | true
        "10.2.8" | 10    | 1     | 9     | false
        "10.2.8" | 10    | 2     | 9     | true
    }

    @Unroll
    def "limitSize"() {
        expect:
        StringUtil.limitSize(input, length) == expected

        where:
        input        | length | expected
        null         | 3      | null
        ""           | 3      | ""
        "abc"        | 3      | "abc"
        "abcde"      | 3      | "..."
        "abcdefghij" | 5      | "ab..."
    }

    def randomIdentifier() {
        expect:
        StringUtil.randomIdentifier(5).length() == 5
        StringUtil.randomIdentifier(5) != StringUtil.randomIdentifier(5)
    }

    @Unroll
    def defaultValueFormatter() {
        expect:
        new StringUtil.DefaultFormatter().toString(input) == expected

        where:
        input                  | expected
        null                   | null
        ""                     | ""
        "abc"                  | "abc"
        Object                 | "java.lang.Object"
        new String[0]          | null
        ["a", "b"] as String[] | "[a, b]"
        []                     | null
        ["a", "b"]             | "[a, b]"
    }

    @Unroll
    def equalsIgnoreCaseAndEmpty() {
        expect:
        StringUtil.equalsIgnoreCaseAndEmpty(s1, s2) == expected

        where:
        s1   | s2      | expected
        ""   | ""      | true
        "a"  | "b"     | false
        "  " | "     " | true
        "  " | null    | true
        null | null    | true
        null | " "     | true
        "a"  | null    | false
        null | "a"     | false
        "a"  | "a"     | true
        "a"  | "A"     | true
    }


    @Unroll
    def "trimRight"() {
        expect:
        StringUtil.trimRight(value) == expected

        where:
        value   | expected
        null    | null
        ""      | ""
        "a   "  | "a"
        "   a " | "   a"
        "\na\n" | "\na"
    }

    @Unroll
    def getLastBlockComment() {
        expect:
        StringUtil.getLastBlockComment(sql) == expected

        where:
        sql                                    | expected
        null                                   | null
        "abc"                                  | null
        "select * from x"                      | null
        "select * from /* a comment here */ x" | null
        "select * from /* a comment here */"   | "/* a comment here */"
    }

    @Unroll
    def getLastLineComment() {
        expect:
        StringUtil.getLastLineComment(sql) == expected

        where:
        sql                               | expected
        null                              | null
        "abc"                             | null
        "select * from x"                 | null
        "select * from -- a comment here" | "-- a comment here"
        "select * from -- a comment here\n--" | "--"
        "select * from -- a comment here\n--foobar\n--" | "--"
    }

    @Unroll
    def stripSqlCommentsAndWhitespacesFromTheEnd() {
        expect:
        StringUtil.stripSqlCommentsAndWhitespacesFromTheEnd(sql) == expected

        where:
        sql                                    | expected
        null                                   | null
        "abc"                                  | "abc"
        "select * from x"                      | "select * from x"
        "select * from -- a comment here"      | "select * from"
        "select * from /* a comment here */ x" | "select * from /* a comment here */ x"
        "select * from /* a comment here */"   | "select * from"

    }

    @Unroll
    def equalsNullWord() {
        expect:
        StringUtil.equalsWordNull(input) == expected

        where:
        input     | expected
        null      | false
        "abc"     | false
        "null"    | true
        "NULL"    | true
        "  null " | false

    }

    @Unroll
    def "stripEnclosingQuotes"() {
        expect:
        StringUtil.stripEnclosingQuotes(input) == expected

        where:
        input           | expected
        ""              | ""
        "a"             | "a"
        "\""            | "\""
        "\"testValue\"" | "testValue"
        "'testValue'"   | "testValue"
        "\"testValue"   | "\"testValue"
    }

    @Unroll
    def "wrap"() {
        expect:
        StringUtil.wrap(input, point, padding) == expected

        where:
        input           | point | padding | expected
        null            | 5     | 3       | null
        "a b c d e f g" | 5     | 3       | "a b c${System.lineSeparator()}   d e f${System.lineSeparator()}   g"
        "abcdefg"       | 5     | 3       | "abcdefg"
        "abc defg"      | 5     | 3       | "abc${System.lineSeparator()}   defg"
    }

    @Unroll
    def "splitCamelCase"() {
        expect:
        StringUtil.splitCamelCase(input) == expected

        where:
        input       | camel | expected
        null        | true  | null
        null        | false | null
        ""          | true  | []
        "abc"       | true  | ["abc"]
        "aBc"       | true  | ["a", "Bc"]
        "aBcdeFghI" | true  | ["a", "Bcde", "Fgh", "I"]
        "abCDef"    | true  | ["ab", "C", "Def"]
    }

    @Unroll
    def getBytesWithEncoding() {
        expect:
        StringUtil.getBytesWithEncoding(input) == expected

        where:
        input | expected
        null  | null
        "abc" | [97, 98, 99] as byte[]

    }

    @Unroll
    def "isNumeric"() {
        expect:
        StringUtil.isNumeric(input) == expected

        where:
        input  | expected
        null   | false
        ""     | false
        "1.0s" | false
        "-1"   | false
        "1.0"  | false
        "1"    | true
    }

    @Unroll
    def "isEmpty"() {
        expect:
        StringUtil.isEmpty(input) == expected

        where:
        input | expected
        null  | true
        ""    | true
        "s"   | false
    }

    @Unroll
    def "splitToChunks"() {
        expect:
        StringUtil.splitToChunks(input, 10) == expected

        where:
        input | expected
        "hello" | ["hello"]
        "hellohello" | ["hellohello"]
        "hellohellohello" | ["hellohello", "hello"]
        "hellohellohellohellohellohello" | ["hellohello", "hellohello", "hellohello"]
    }
}
