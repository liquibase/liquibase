package liquibase.structure.core

import liquibase.util.StringClauses
import spock.lang.Specification
import spock.lang.Unroll

class DataTypeTest extends Specification {

    @Unroll("#featureName: '#string'")

    def "parse works"() {
        when:
        def dataType = DataType.parse(string)

        then:
        dataType.name == expectedName
        dataType.parameters == expectedParams
        dataType.clausesBeforeParameters.toArray(true) == expectedBeforeClause as String[]
        dataType.clausesAfterParameters.toArray(true) == expectedAfterClause as String[]
        dataType.standardType == expectedStandardType

        where:
        string                    | expectedName | expectedParams | expectedBeforeClause | expectedAfterClause | expectedStandardType
        ""                        | null         | []             | []                   | []                  | null
        "int"                     | "int"        | []             | []                   | []                  | DataType.StandardType.INTEGER
        "varchar(10)"             | "varchar"    | ["10"]         | []                   | []                  | DataType.StandardType.VARCHAR
        "varchar2(100 bytes)"     | "varchar2"   | ["100 bytes"]  | []                   | []                  | null
        "double(2, 15)"           | "double"     | ["2", "15"]    | []                   | []                  | DataType.StandardType.DOUBLE
        "double ( 2,15 )"         | "double"     | ["2", "15"]    | []                   | []                  | DataType.StandardType.DOUBLE
        "timestamp with timezone" | "timestamp"  | []             | ["with timezone"]    | []                  | DataType.StandardType.TIMESTAMP
    }

    @Unroll("#featureName: #expected")
    def "toString works"() {
        expect:
        dataType.toString() == expected

        where:
        dataType                                                                                                                                    | expected
        new DataType()                                                                                                                              | ""
        new DataType("int")                                                                                                                         | "int"
        new DataType("INT")                                                                                                                         | "INT"
        new DataType("two strings")                                                                                                                 | "two strings"
        new DataType("int", "5")                                                                                                                    | "int(5)"
        new DataType("varchar", "5 bytes")                                                                                                          | "varchar(5 bytes)"
        new DataType("int", "5", "12")                                                                                                              | "int(5, 12)"
        new DataType("int", new StringClauses(" ").append("a").append("b"), null, new StringClauses(" ").append("x").append("y"))                   | "int a b x y"
        new DataType("int", new StringClauses(" ").append("a").append("b"), ["1", "2"] as String[], new StringClauses(" ").append("x").append("y")) | "int a b (1, 2) x y"
        new DataType(DataType.StandardType.VARCHAR, 10)                                                                                             | "VARCHAR(10)"
    }

    @Unroll("#featureName: #name")
    def standardType() {
        expect:
        DataType.standardType(name) == expected

        where:
        name                     | expected
        "integer"                | DataType.StandardType.INTEGER
        "INTEGER"                | DataType.StandardType.INTEGER
        "int"                    | DataType.StandardType.INTEGER
        "varchar"                | DataType.StandardType.VARCHAR
        "datetime"               | DataType.StandardType.TIMESTAMP
        "datetime with timezone" | DataType.StandardType.TIMESTAMPZ
        "time with timezone"     | DataType.StandardType.TIMEZ
        "fake"                   | null
    }
}
