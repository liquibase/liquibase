package liquibase.database.core

import liquibase.structure.core.Column
import liquibase.structure.core.Table
import spock.lang.Specification
import spock.lang.Unroll

import static liquibase.database.ObjectQuotingStrategy.QUOTE_ALL_OBJECTS
import static liquibase.database.ObjectQuotingStrategy.QUOTE_ONLY_RESERVED_WORDS

class SnowflakeDatabaseSpec extends Specification {

    @Unroll("#featureName [#quotingStrategy], [#objectName, #objectType], [#expectedCorrect, #expectedEscape]")
    def "correctObjectName, escapeObjectName"() {
        given:
        def database = new SnowflakeDatabase().tap {
            if (quotingStrategy != null) {
                it.objectQuotingStrategy = quotingStrategy
            }
        }

        expect:
        verifyAll {
            database.correctObjectName(objectName, objectType) == expectedCorrect
            database.escapeObjectName(objectName, objectType) == expectedEscape
        }

        where:
        quotingStrategy           || objectName       | objectType || expectedCorrect  | expectedEscape
        null                      || 'col1'           | Column     || 'COL1'           | 'col1'
        null                      || 'COL1'           | Column     || 'COL1'           | 'COL1'
        null                      || 'Col1'           | Column     || 'COL1'           | 'Col1'
        null                      || 'col with space' | Column     || 'COL WITH SPACE' | '"col with space"'
        QUOTE_ONLY_RESERVED_WORDS || 'col1'           | Column     || 'COL1'           | 'col1'
        QUOTE_ONLY_RESERVED_WORDS || 'COL1'           | Column     || 'COL1'           | 'COL1'
        QUOTE_ONLY_RESERVED_WORDS || 'Col1'           | Column     || 'COL1'           | 'Col1'
        QUOTE_ONLY_RESERVED_WORDS || 'col with space' | Column     || 'COL WITH SPACE' | '"col with space"'
        QUOTE_ALL_OBJECTS         || 'col1'           | Column     || 'col1'           | '"col1"'
        QUOTE_ALL_OBJECTS         || 'COL1'           | Column     || 'COL1'           | '"COL1"'
        QUOTE_ALL_OBJECTS         || 'Col1'           | Column     || 'Col1'           | '"Col1"'
        QUOTE_ALL_OBJECTS         || 'col with space' | Column     || 'col with space' | '"col with space"'
    }

    @Unroll()
    def "should return a properly escaped string for the database"() {
        given:
        def database = new SnowflakeDatabase()

        expect:
        database.escapeStringForDatabase(unescapedString) == escapedString

        where:
        unescapedString || escapedString
        null            || null
        "'col'name'"    || "\\'col\\'name\\'"
        "'col\\'name'"  || "\\'col\\'name\\'"
        "\\'col'name'"  || "\\'col\\'name\\'"
        "'col'name\\'"  || "\\'col\\'name\\'"
        "col 'name"     || "col \\'name"
        "it's a col"    || "it\\'s a col"
        "\\'col'"       || "\\'col\\'"
        "''"            || "\\'\\'"
    }

    def "Should escape object name correctly with catalog and schema combinations"() {
        given:
        def database = new SnowflakeDatabase()

        when:
        def result = database.escapeObjectName(catalogName, schemaName, objectName, Table.class)

        then:
        result == expectedResult

        where:
        catalogName | schemaName | objectName | expectedResult
        null        | null       | "myTable"  | "myTable"
        null        | "mySchema" | "myTable"  | "mySchema.myTable"
        "myCatalog" | "mySchema" | "myTable"  | "myCatalog.mySchema.myTable"
        "myCatalog" | null       | "myTable"  | "myCatalog.PUBLIC.myTable"  // null schema defaults to PUBLIC
        null        | "schema"   | "myTable"  | '"schema".myTable'  // "schema" is reserved word, gets quoted
        "catalog"   | "schema"   | "myTable"  | 'catalog."schema".myTable'  // reserved word with catalog
        "my_db"     | "my_sch"   | "my_tbl"   | "my_db.my_sch.my_tbl"
    }
}
