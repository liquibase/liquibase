package liquibase.util

import org.hamcrest.Matchers
import spock.lang.Specification
import spock.lang.Unroll

import static spock.util.matcher.HamcrestSupport.that

class StringUtilTest extends Specification {

    @Unroll
    def "processMultilineSql examples"() {
        expect:
        that Arrays.asList(StringUtil.processMutliLineSQL(rawString, stripComments, splitStatements, endDelimiter)), Matchers.contains(expected.toArray())

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
        true          | true            | "//"         | "drop procedure if exists my_proc//\n\ncreate procedure my_proc(i_myvar varchar)\nbegin\n  a bunch of code here\nend//"                                                                          | ["drop procedure if exists my_proc", "create procedure my_proc(i_myvar varchar)\nbegin\n  a bunch of code here\nend"]
        true          | true            | "/"          | "CREATE OR REPLACE PACKAGE emp_actions AS  -- spec\nTYPE EmpRecTyp IS RECORD (emp_id INT, salary REAL);\nCURSOR desc_salary RETURN EmpRecTyp);\nEND emp_actions;"                                   | ["CREATE OR REPLACE PACKAGE emp_actions AS  \nTYPE EmpRecTyp IS RECORD (emp_id INT, salary REAL);\nCURSOR desc_salary RETURN EmpRecTyp);\nEND emp_actions;"]
        true          | true            | "/"          | "CREATE OR REPLACE PACKAGE emp_actions AS  -- spec\nTYPE EmpRecTyp IS RECORD (emp_id INT, salary REAL);\nCURSOR desc_salary RETURN EmpRecTyp);\nEND emp_actions;\n/\nanother statement;here\n/\n"   | ["CREATE OR REPLACE PACKAGE emp_actions AS  \nTYPE EmpRecTyp IS RECORD (emp_id INT, salary REAL);\nCURSOR desc_salary RETURN EmpRecTyp);\nEND emp_actions;", "another statement;here"]
        true          | true            | "\\n/"       | "CREATE OR REPLACE PACKAGE emp_actions AS  -- spec\nTYPE EmpRecTyp IS RECORD (emp_id INT, salary REAL);\nCURSOR desc_salary RETURN EmpRecTyp);\nEND emp_actions;\n/\nanother statement;here\n/\n"   | ["CREATE OR REPLACE PACKAGE emp_actions AS  \nTYPE EmpRecTyp IS RECORD (emp_id INT, salary REAL);\nCURSOR desc_salary RETURN EmpRecTyp);\nEND emp_actions;", "another statement;here"]
        true          | true            | "\\ngo"      | "CREATE OR REPLACE PACKAGE emp_actions AS  -- spec\nTYPE EmpRecTyp IS RECORD (emp_id INT, salary REAL);\nCURSOR desc_salary RETURN EmpRecTyp);\nEND emp_actions;\nGO\nanother statement;here\nGO\n" | ["CREATE OR REPLACE PACKAGE emp_actions AS  \nTYPE EmpRecTyp IS RECORD (emp_id INT, salary REAL);\nCURSOR desc_salary RETURN EmpRecTyp);\nEND emp_actions;", "another statement;here"]
        true          | true            | null         | "statement 1;\nstatement 2;\nGO\n\nstatement 3; statement 4;"                                                                                                                                       | ["statement 1", "statement 2", "statement 3", "statement 4"]
        true          | true            | "\\nGO"      | "statement 1 \nGO\nstatement 2"                                                                                                                                                                     | ["statement 1", "statement 2"]
        true          | true            | "\\nGO"      | "CREATE OR REPLACE PACKAGE emp_actions AS  -- spec\nTYPE EmpRecTyp IS RECORD (emp_id INT, salary REAL);\nCURSOR desc_salary RETURN EmpRecTyp);\nEND emp_actions;\nGO\nanother statement;here\nGO\n" | ["CREATE OR REPLACE PACKAGE emp_actions AS  \nTYPE EmpRecTyp IS RECORD (emp_id INT, salary REAL);\nCURSOR desc_salary RETURN EmpRecTyp);\nEND emp_actions;", "another statement;here"]

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
        base | addition | expected
        "abc" | "_xyz" | "abc_xyz"
        "abc" | "_XYZ" | "abc_xyz"
        "abc" | "_XyZ" | "abc_xyz"

        "ABC" | "_xyz" | "ABC_XYZ"
        "ABC" | "_XYZ" | "ABC_XYZ"
        "ABC" | "_XyZ" | "ABC_XYZ"

        "AbC" | "_xyz" | "AbC_xyz"
        "AbC" | "_XYZ" | "AbC_XYZ"
        "AbC" | "_XyZ" | "AbC_XyZ"

        "a1" | "_x" | "a1_x"
        "A1" | "_x" | "A1_X"
        "123" | "_x" | "123_x"
        "123" | "_X" | "123_X"
    }
}
