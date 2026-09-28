package liquibase.precondition.core

import liquibase.database.Database
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.exception.PreconditionFailedException
import spock.lang.Specification

import java.sql.Connection
import java.sql.DriverManager

class ColumnExistsPreconditionTest extends Specification {

    def "columnExists finds an H2 column when catalogName is the schema name"() {
        given:
        def database = h2Database()
        createTable(database)
        def precondition = columnExists("eocs", "eocs", "ex_incident_type", "e_number_range_id")

        when:
        precondition.check(database, null, null, null)

        then:
        noExceptionThrown()

        cleanup:
        database?.close()
    }

    def "columnExists still fails when the H2 column is absent and catalogName is set"() {
        given:
        def database = h2Database()
        createTable(database)
        def precondition = columnExists("eocs", "eocs", "ex_incident_type", "missing_column")

        when:
        precondition.check(database, null, null, null)

        then:
        thrown(PreconditionFailedException)

        cleanup:
        database?.close()
    }

    def "columnExists finds an H2 column when only schemaName is set"() {
        given:
        def database = h2Database()
        createTable(database)
        def precondition = columnExists(null, "eocs", "ex_incident_type", "e_number_range_id")

        when:
        precondition.check(database, null, null, null)

        then:
        noExceptionThrown()

        cleanup:
        database?.close()
    }

    def "columnExists finds an H2 column whose name cannot use the fast check when catalogName is set"() {
        given:
        def database = h2Database()
        def statement = ((JdbcConnection) database.connection).createStatement()
        try {
            statement.execute("CREATE TABLE eocs.ex_incident_type (e_id VARCHAR(36) PRIMARY KEY, _id VARCHAR(36))")
        } finally {
            statement.close()
        }
        def precondition = columnExists("eocs", "eocs", "ex_incident_type", "_id")

        when:
        precondition.check(database, null, null, null)

        then:
        noExceptionThrown()

        cleanup:
        database?.close()
    }

    def "columnExists still fails for a snapshot-only H2 name when the column is absent"() {
        given:
        def database = h2Database()
        createTable(database)
        def precondition = columnExists("eocs", "eocs", "ex_incident_type", "_missing")

        when:
        precondition.check(database, null, null, null)

        then:
        thrown(PreconditionFailedException)

        cleanup:
        database?.close()
    }

    private static ColumnExistsPrecondition columnExists(String catalogName, String schemaName, String tableName, String columnName) {
        def precondition = new ColumnExistsPrecondition()
        precondition.catalogName = catalogName
        precondition.schemaName = schemaName
        precondition.tableName = tableName
        precondition.columnName = columnName
        return precondition
    }

    private static Database h2Database() {
        def url = "jdbc:h2:mem:repro${System.nanoTime()};DB_CLOSE_DELAY=-1;INIT=CREATE SCHEMA IF NOT EXISTS eocs\\;SET SCHEMA eocs"
        Connection connection = DriverManager.getConnection(url, "sa", "")
        return DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection))
    }

    private static void createTable(Database database) {
        def statement = ((JdbcConnection) database.connection).createStatement()
        try {
            statement.execute("CREATE TABLE eocs.ex_incident_type (e_id VARCHAR(36) PRIMARY KEY, e_number_range_id VARCHAR(36))")
        } finally {
            statement.close()
        }
    }
}
