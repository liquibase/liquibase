package liquibase.database.core

import liquibase.database.jvm.JdbcConnection
import liquibase.structure.core.Column
import spock.lang.Specification
import spock.lang.Unroll

import java.sql.ResultSet
import java.sql.Statement

import static liquibase.database.ObjectQuotingStrategy.QUOTE_ALL_OBJECTS
import static liquibase.database.ObjectQuotingStrategy.QUOTE_ONLY_RESERVED_WORDS

class EnterpriseDBDatabaseTest extends Specification {

    @Unroll("#featureName [#quotingStrategy], [#objectName, #objectType], [#expectedCorrect, #expectedEscape]")
    def "correctObjectName, escapeObjectName"() {
        given:
        def database = new EnterpriseDBDatabase().tap {
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

    @Unroll
    def "isCorrectDatabaseImplementation"() {
        when:
        def conn = Mock(JdbcConnection)
        def stmt = Mock(Statement)
        def rs = Mock(ResultSet)

        conn.getURL() >> url
        conn.createStatement() >> stmt
        stmt.executeQuery("select version()") >> rs
        rs.next() >> true
        rs.getObject(1) >> version

        then:
        new EnterpriseDBDatabase().isCorrectDatabaseImplementation(conn) == expected

        where:
        url                                   | version                                                                                                                                         | expected
        "jdbc:edb://localhost:50000/db"       | "PostgreSQL 11.0 (EnterpriseDB Advanced Server 11.0.0) on x86_64-pc-linux-gnu, compiled by gcc (GCC) 4.8.5 20150623 (Red Hat 4.8.5-11), 64-bit" | true
        "jdbc:postgresql://localhost:5432/db" | "PostgreSQL 11.0 (EnterpriseDB Advanced Server 11.0.0) on x86_64-pc-linux-gnu, compiled by gcc (GCC) 4.8.5 20150623 (Red Hat 4.8.5-11), 64-bit" | true
        "jdbc:edb://localhost:50000/db"       | "PostgreSQL 9.3.10 on x86_64-unknown-linux-gnu, compiled by gcc (Ubuntu 4.8.2-19ubuntu1) 4.8.2, 64-bit"                                         | false
    }
}
