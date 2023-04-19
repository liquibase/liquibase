package liquibase.database.core

import liquibase.database.jvm.JdbcConnection
import spock.lang.Specification
import spock.lang.Unroll

import java.sql.ResultSet
import java.sql.Statement

class EnterpriseDBDatabaseTest extends Specification {

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
