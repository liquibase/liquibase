package liquibase.database.core

import liquibase.database.DatabaseConnection
import liquibase.structure.core.Column
import spock.lang.Specification
import spock.lang.Unroll

import static liquibase.database.ObjectQuotingStrategy.QUOTE_ALL_OBJECTS
import static liquibase.database.ObjectQuotingStrategy.QUOTE_ONLY_RESERVED_WORDS

class CockroachDatabaseTest extends Specification {

    @Unroll("#featureName [#quotingStrategy], [#objectName, #objectType], [#expectedCorrect, #expectedEscape]")
    def "correctObjectName, escapeObjectName"() {
        given:
        def database = new CockroachDatabase().tap {
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
        null                      || 'col1'           | Column     || 'col1'           | 'col1'
        null                      || 'COL1'           | Column     || 'col1'           | 'COL1'
        null                      || 'Col1'           | Column     || 'Col1'           | '"Col1"'
        null                      || 'col with space' | Column     || 'col with space' | '"col with space"'
        QUOTE_ONLY_RESERVED_WORDS || 'col1'           | Column     || 'col1'           | 'col1'
        QUOTE_ONLY_RESERVED_WORDS || 'COL1'           | Column     || 'col1'           | 'COL1'
        QUOTE_ONLY_RESERVED_WORDS || 'Col1'           | Column     || 'col1'           | 'Col1'
        QUOTE_ONLY_RESERVED_WORDS || 'col with space' | Column     || 'col with space' | '"col with space"'
        QUOTE_ALL_OBJECTS         || 'col1'           | Column     || 'col1'           | '"col1"'
        QUOTE_ALL_OBJECTS         || 'COL1'           | Column     || 'COL1'           | '"COL1"'
        QUOTE_ALL_OBJECTS         || 'Col1'           | Column     || 'Col1'           | '"Col1"'
        QUOTE_ALL_OBJECTS         || 'col with space' | Column     || 'col with space' | '"col with space"'
    }

    def "postgres-family capabilities"() {
        given:
        def database = new CockroachDatabase()

        expect:
        verifyAll {
            database.supportsEnumTypeSnapshot()              // inherited from PostgresDatabase
            !database.supportsCompositeTypeSnapshot()        // not snapshot-able on CockroachDB
            database.supportsCheckConstraintSnapshot()       // inherited from PostgresDatabase
            !database.supportsStoredLogicSnapshot()          // no stored-logic catalog
        }
    }

    def useSerialDatatypes() {
        when:
        def db = new CockroachDatabase()
        db.databaseMajorVersion = majorVersion
        db.databaseMinorVersion = minorVersion

        then:
        db.useSerialDatatypes() == expected

        where:
        majorVersion | minorVersion | expected
        20           | 0            | true
        20           | 1            | true
        20           | 2            | true
        21           | 0            | true
        21           | 1            | true
        21           | 2            | false
        21           | 3            | false
        22           | 0            | false
    }

    def "isCorrectDatabaseImplementation returns false when connection URL is null"() {
        given:
        def conn = Mock(DatabaseConnection)
        conn.getURL() >> null

        when:
        def result = new CockroachDatabase().isCorrectDatabaseImplementation(conn)

        then:
        result == false
    }

    def "isCorrectDatabaseImplementation returns false when connection URL does not match postgres patterns"() {
        given:
        def conn = Mock(DatabaseConnection)
        conn.getURL() >> "jdbc:oracle:thin:@localhost:1521:orcl"

        when:
        def result = new CockroachDatabase().isCorrectDatabaseImplementation(conn)

        then:
        result == false
    }
}
