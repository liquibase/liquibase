package liquibase.sqlgenerator.core

import liquibase.Scope
import liquibase.changelog.ChangeSet
import liquibase.database.core.*
import liquibase.sdk.resource.MockResourceAccessor
import liquibase.sqlgenerator.MockSqlGeneratorChain
import liquibase.statement.core.MarkChangeSetRanStatement
import spock.lang.Specification
import spock.lang.Unroll

class MarkChangeSetRanGeneratorEscapingTest extends Specification {

    String generateInsertSql(ChangeSet changeSet, database) {
        def scopeValues = [(Scope.Attr.resourceAccessor.name()): new MockResourceAccessor()]
        Scope.child(scopeValues, {
            new MarkChangeSetRanGenerator().generateSql(
                    new MarkChangeSetRanStatement(changeSet, ChangeSet.ExecType.EXECUTED),
                    database,
                    new MockSqlGeneratorChain())[0].toSql()
        } as Scope.ScopedRunnerWithReturn<String>)
    }

    @Unroll
    def "INSERT SQL escapes single quotes in changeset fields - #databaseName"() {
        given:
        def changeSet = new ChangeSet("it's-an-id", "dev'author", false, false, "path/my'file.xml", null, null, null)

        when:
        String sql = generateInsertSql(changeSet, database)

        then:
        sql.contains("'it''s-an-id'")
        sql.contains("'dev''author'")
        sql.contains("'path/my''file.xml'")

        where:
        database               | databaseName
        new H2Database()       | "H2"
        new PostgresDatabase() | "PostgreSQL"
        new OracleDatabase()   | "Oracle"
        new MSSQLDatabase()    | "MSSQL"
        new MySQLDatabase()    | "MySQL"
        new MariaDBDatabase()  | "MariaDB"
    }

    @Unroll
    def "INSERT SQL neutralizes statement terminator injection in changeset id - #databaseName"() {
        given:
        def changeSet = new ChangeSet("'; DROP TABLE DATABASECHANGELOG--", "author", false, false, "changelog.xml", null, null, null)

        when:
        String sql = generateInsertSql(changeSet, database)

        then:
        // The injected quote is doubled — the whole value becomes a safe SQL string literal with no statement break
        sql.contains("'''; DROP TABLE DATABASECHANGELOG--'")

        where:
        database               | databaseName
        new H2Database()       | "H2"
        new PostgresDatabase() | "PostgreSQL"
        new OracleDatabase()   | "Oracle"
        new MSSQLDatabase()    | "MSSQL"
        new MySQLDatabase()    | "MySQL"
        new MariaDBDatabase()  | "MariaDB"
    }

    @Unroll
    def "INSERT SQL neutralizes boolean bypass injection in changeset author - #databaseName"() {
        given:
        def changeSet = new ChangeSet("id", "' OR '1'='1", false, false, "changelog.xml", null, null, null)

        when:
        String sql = generateInsertSql(changeSet, database)

        then:
        sql.contains("''' OR ''1''=''1'")

        where:
        database               | databaseName
        new H2Database()       | "H2"
        new PostgresDatabase() | "PostgreSQL"
        new OracleDatabase()   | "Oracle"
        new MSSQLDatabase()    | "MSSQL"
        new MySQLDatabase()    | "MySQL"
        new MariaDBDatabase()  | "MariaDB"
    }
}