package liquibase.util

import liquibase.database.core.*
import liquibase.statement.DatabaseFunction
import liquibase.structure.core.DataType
import spock.lang.Specification
import spock.lang.Unroll

import java.sql.Timestamp
import java.sql.Types

import static liquibase.database.ObjectQuotingStrategy.QUOTE_ALL_OBJECTS
import static liquibase.database.ObjectQuotingStrategy.QUOTE_ONLY_RESERVED_WORDS

/**
 * Tests to ensure correction functionality of SqlUtil
 */
class SqlUtilTest extends Specification {

    def isNumeric() {
        expect:
        SqlUtil.isNumeric(input) == expected

        where:
        input          | expected
        Types.BIGINT   | true
        Types.BIT      | true
        Types.INTEGER  | true
        Types.SMALLINT | true
        Types.TINYINT  | true
        Types.DECIMAL  | true
        Types.DOUBLE   | true
        Types.FLOAT    | true
        Types.NUMERIC  | true
        Types.REAL     | true
        Types.VARCHAR  | false
    }

    def isBoolean() {
        expect:
        SqlUtil.isBoolean(input) == expected

        where:
        input         | expected
        Types.BOOLEAN | true
        Types.BIT     | false
        Types.INTEGER | false
        Types.VARCHAR | false
    }

    def isDate() {
        expect:
        SqlUtil.isDate(input) == expected

        where:
        input                         | expected
        Types.DATE                    | true
        Types.TIME                    | true
        Types.TIMESTAMP               | true
        Types.TIME_WITH_TIMEZONE      | true
        Types.TIMESTAMP_WITH_TIMEZONE | true
        Types.VARCHAR                 | false
    }

    @Unroll("SqlUtil.parseValue(#value on #db as #dataType)")
    def "ParseValue"() {
        when:
        def type = new DataType(dataType)
        type.setDataTypeId(dataTypeId)

        def result = SqlUtil.parseValue(db, value, type)

        then:
        result == expectedObject

        where:
        value                                                     | db                     | dataType        | expectedObject                            | dataTypeId
        (int) 3                                                   | new OracleDatabase()   | "int"           | (Integer) 3                               | null
        ""                                                        | new OracleDatabase()   | "char(3)"       | ""                                        | 23
        ""                                                        | new OracleDatabase()   | "int"           | null                                      | null
        "to_date('1992-06-26 12:15:23', 'YYYY-MM-DD HH24:MI:SS')" | new OracleDatabase()   | "timestamp"     | new Timestamp(92, 05, 26, 12, 15, 23, 0)  | Types.TIMESTAMP
        "to_date('1992-06-26', 'YYYY-MM-DD')"                     | new OracleDatabase()   | "date"          | new java.sql.Date(92, 05, 26)             | Types.DATE
        "custom_function()"                                       | new OracleDatabase()   | "date"          | new DatabaseFunction("custom_function()") | Types.DATE
        "2013-05-16"                                              | new H2Database()       | "date"          | new java.sql.Date(113, 04, 16)            | Types.DATE
        "'a string'"                                              | new H2Database()       | "char(3)"       | "a string"                                | Types.CHAR
        "'a string'"                                              | new H2Database()       | "nchar(3)"      | "a string"                                | Types.NCHAR
        "((a string))"                                            | new H2Database()       | "char(3)"       | "a string"                                | Types.CHAR
        "('a string')"                                            | new H2Database()       | "char(3)"       | "a string"                                | Types.CHAR
        "(a string)"                                              | new H2Database()       | "char(3)"       | new DatabaseFunction("a string")          | Types.CHAR
        "[array literal]"                                         | new H2Database()       | "array"         | new DatabaseFunction("[array literal]")   | Types.ARRAY
        "362"                                                     | new H2Database()       | "bigint"        | new BigInteger("362")                     | Types.BIGINT
        "not a number"                                            | new H2Database()       | "bigint"        | new DatabaseFunction("not a number")      | Types.BIGINT
        "[binary literal]"                                        | new H2Database()       | "binary"        | new DatabaseFunction("[binary literal]")  | Types.BINARY
        "[binary literal]"                                        | new H2Database()       | "varbinary"     | new DatabaseFunction("[binary literal]")  | Types.VARBINARY
        "[binary literal]"                                        | new H2Database()       | "longvarbinary" | new DatabaseFunction("[binary literal]")  | Types.LONGVARBINARY
        "b'0'"                                                    | new MySQLDatabase()    | "bit"           | "0"                                       | Types.BIT
        "b'1'"                                                    | new MySQLDatabase()    | "bit"           | "1"                                       | Types.BIT
        "B'0'::\"bit\""                                           | new PostgresDatabase() | "bit"           | "0"                                       | Types.BIT
        "B'1'::\"bit\""                                           | new PostgresDatabase() | "bit"           | "1"                                       | Types.BIT
        "0"                                                       | new H2Database()       | "bit"           | 0                                         | Types.BIT
        "1"                                                       | new H2Database()       | "bit"           | 1                                         | Types.BIT
        "false"                                                   | new H2Database()       | "bit"           | false                                     | Types.BIT
        "true"                                                    | new H2Database()       | "bit"           | true                                      | Types.BIT
        "false"                                                   | new MSSQLDatabase()    | "bit"           | new DatabaseFunction("'false'")           | Types.BIT
        "true"                                                    | new MSSQLDatabase()    | "bit"           | new DatabaseFunction("'true'")            | Types.BIT
        "0"                                                       | new MSSQLDatabase()    | "bit"           | new DatabaseFunction("'false'")           | Types.BIT
        "1"                                                       | new MSSQLDatabase()    | "bit"           | new DatabaseFunction("'true'")            | Types.BIT
        "whatever"                                                | new MSSQLDatabase()    | "bit"           | new DatabaseFunction("'whatever'")        | Types.BIT
        "'a string'"                                              | new H2Database()       | "blob"          | "a string"                                | Types.BLOB
        "a_function()"                                            | new H2Database()       | "blob"          | new DatabaseFunction("a_function()")      | Types.BLOB
        "true"                                                    | new H2Database()       | "boolean"       | true                                      | Types.BOOLEAN
        "false"                                                   | new H2Database()       | "boolean"       | false                                     | Types.BOOLEAN
        "a_function()"                                            | new H2Database()       | "boolean"       | new DatabaseFunction("a_function()")      | Types.BOOLEAN
        "'a string'"                                              | new H2Database()       | "clob"          | "a string"                                | Types.CLOB
        "'a string'"                                              | new H2Database()       | "nclob"         | "a string"                                | Types.NCLOB
        "'a string'"                                              | new H2Database()       | "varchar"       | "a string"                                | Types.VARCHAR
        "'a string'"                                              | new H2Database()       | "nvarchar"      | "a string"                                | Types.NVARCHAR
        "'a string'"                                              | new H2Database()       | "longnvarchar"  | "a string"                                | Types.LONGNVARCHAR
        "'a string'"                                              | new H2Database()       | "longvarchar"   | "a string"                                | Types.LONGVARCHAR
        "a_function()"                                            | new H2Database()       | "datalink"      | new DatabaseFunction("a_function()")      | Types.DATALINK
        "1234"                                                    | new H2Database()       | "int"           | 1234                                      | Types.INTEGER
        "a_function()"                                            | new H2Database()       | "int"           | new DatabaseFunction("a_function()")      | Types.INTEGER
        "1234"                                                    | new H2Database()       | "smallint"      | 1234                                      | Types.SMALLINT
        "a_function()"                                            | new H2Database()       | "smallint"      | new DatabaseFunction("a_function()")      | Types.SMALLINT
        "12"                                                      | new H2Database()       | "tinyint"       | 12                                        | Types.TINYINT
        "a_function()"                                            | new H2Database()       | "tinyint"       | new DatabaseFunction("a_function()")      | Types.TINYINT
        "12.34"                                                   | new H2Database()       | "decimal"       | 12.34                                     | Types.DECIMAL
        "a_function()"                                            | new H2Database()       | "decimal"       | new DatabaseFunction("a_function()")      | Types.DECIMAL
        "12.34"                                                   | new H2Database()       | "double"        | 12.34                                     | Types.DOUBLE
        "a_function()"                                            | new H2Database()       | "double"        | new DatabaseFunction("a_function()")      | Types.DOUBLE
        "12.34"                                                   | new H2Database()       | "float"         | 12.34F                                    | Types.FLOAT
        "a_function()"                                            | new H2Database()       | "float"         | new DatabaseFunction("a_function()")      | Types.FLOAT
        "12.34"                                                   | new H2Database()       | "real"          | 12.34F                                    | Types.REAL
        "a_function()"                                            | new H2Database()       | "real"          | new DatabaseFunction("a_function()")      | Types.REAL
        "a_function()"                                            | new H2Database()       | "distinct"      | new DatabaseFunction("a_function()")      | Types.DISTINCT
        "a_function()"                                            | new H2Database()       | "object"        | new DatabaseFunction("a_function()")      | Types.JAVA_OBJECT
        "any value"                                               | new H2Database()       | "null"          | null                                      | Types.NULL
        "12.0"                                                    | new MSSQLDatabase()    | "numeric"       | new DatabaseFunction("12.0")              | Types.NUMERIC
        "12.00"                                                   | new MSSQLDatabase()    | "numeric"       | new DatabaseFunction("12.00")             | Types.NUMERIC
        "12.000"                                                  | new MSSQLDatabase()    | "numeric"       | new DatabaseFunction("12.000")            | Types.NUMERIC
        "''"                                                      | new MSSQLDatabase()    | "numeric"       | new DatabaseFunction("''")                | Types.NUMERIC
        "12.0"                                                    | new H2Database()       | "numeric"       | 12                                        | Types.NUMERIC
        "a_function()"                                            | new H2Database()       | "numeric"       | new DatabaseFunction("a_function()")      | Types.NUMERIC
        "12.35"                                                   | new DB2Database()      | "decfloat"      | 12.35                                     | Types.OTHER
        "12.35"                                                   | new H2Database()       | "decfloat"      | new DatabaseFunction("12.35")             | Types.OTHER
        "a_function()"                                            | new H2Database()       | "ref"           | new DatabaseFunction("a_function()")      | Types.REF
        "a_function()"                                            | new H2Database()       | "rowid"         | new DatabaseFunction("a_function()")      | Types.ROWID
        "a_function()"                                            | new H2Database()       | "xml"           | new DatabaseFunction("a_function()")      | Types.SQLXML
        "a_function()"                                            | new H2Database()       | "struct"        | new DatabaseFunction("a_function()")      | Types.STRUCT
        "value here"                                              | new MySQLDatabase()    | "enum_name"     | "value here"                              | Integer.MIN_VALUE
        ""                                                        | new H2Database()       | "unsupported"   | null                                      | Integer.MIN_VALUE
        "''"                                                      | new H2Database()       | "unsupported"   | ""                                        | Integer.MIN_VALUE
        "'something here'"                                        | new H2Database()       | "unsupported"   | new DatabaseFunction("'something here'")  | Integer.MIN_VALUE
        "something here"                                          | new H2Database()       | "unsupported"   | new DatabaseFunction("something here")    | Integer.MIN_VALUE

    }

    @Unroll("#featureName [#quotingStrategy], [#predicate, #columnNames, #parameters], [#expected]")
    def replacePredicatePlaceholders() {
        given:
        def database = new H2Database().tap {
            if (quotingStrategy != null) {
                it.objectQuotingStrategy = quotingStrategy
            }
        }

        expect:
        SqlUtil.replacePredicatePlaceholders(database, predicate, columnNames, parameters) == expected

        where:
        quotingStrategy           || predicate                          | columnNames                | parameters     || expected
        null                      || 'query :name'                      | ['col1']                   | ['value 1']    || 'query col1'
        null                      || 'query :name=:value, :name=:value' | ['col1', 'col with space'] | ['value 1', 6] || 'query col1=\'value 1\', "COL WITH SPACE"=6'
        QUOTE_ONLY_RESERVED_WORDS || 'query :name'                      | ['col1']                   | ['value 1']    || 'query col1'
        QUOTE_ONLY_RESERVED_WORDS || 'query :name=:value, :name=:value' | ['col1', 'col with space'] | ['value 1', 6] || 'query col1=\'value 1\', "COL WITH SPACE"=6'
        QUOTE_ALL_OBJECTS         || 'query :name'                      | ['col1']                   | ['value 1']    || 'query "col1"'
        QUOTE_ALL_OBJECTS         || 'query :name=:value, :name=:value' | ['col1', 'col with space'] | ['value 1', 6] || 'query "col1"=\'value 1\', "col with space"=6'
    }
}
