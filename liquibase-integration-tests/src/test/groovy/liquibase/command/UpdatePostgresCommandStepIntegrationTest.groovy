package liquibase.command

import liquibase.Scope
import liquibase.command.util.CommandUtil
import liquibase.extension.testing.testsystem.DatabaseTestSystem
import liquibase.extension.testing.testsystem.TestSystemFactory
import liquibase.extension.testing.testsystem.spock.LiquibaseIntegrationTest
import spock.lang.Shared
import spock.lang.Specification

import java.sql.Connection
import java.sql.DatabaseMetaData
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement

import static org.junit.jupiter.api.Assertions.assertTrue

@LiquibaseIntegrationTest
class UpdatePostgresCommandStepIntegrationTest extends Specification {

    @Shared
    private DatabaseTestSystem postgres = Scope.currentScope.getSingleton(TestSystemFactory).getTestSystem("postgresql") as DatabaseTestSystem

    def "verify cache is restarted after changes have been deployed"() {
        when:
        CommandUtil.runUpdate(postgres, "changelogs/fast.check.changelog.test.xml", null, null, null)

        then:
        assertTableExists("DATABASECHANGELOG")
        assertTableExists("DATABASECHANGELOGLOCK")
        assertTableExists("DUMMY_TABLE")

        when:
        dropTablesIfExist()
        CommandUtil.runUpdate(postgres, "changelogs/fast.check.changelog.test.xml", null, null, null)

        then:
        assertTableExists("DATABASECHANGELOG")
        assertTableExists("DATABASECHANGELOGLOCK")
        assertTableExists("DUMMY_TABLE")
    }

    private void assertTableExists(String tableName) throws SQLException {
        try (
                Connection connection = postgres.getConnection()
        ) {
            DatabaseMetaData dbm = connection.getMetaData()
            def catalog = connection.getCatalog()
            try (ResultSet tables = dbm.getTables(catalog.toLowerCase(), null, tableName.toLowerCase(), null)) {
                assertTrue(tables.isBeforeFirst())
            }
        }
    }

    private void dropTablesIfExist() throws SQLException {
        try (
                Connection connection = postgres.getConnection()
                Statement statement = connection.createStatement()
        ) {
            statement.execute("DROP TABLE IF EXISTS public.DATABASECHANGELOG")
            statement.execute("DROP TABLE IF EXISTS public.DATABASECHANGELOGLOCK")
            statement.execute("DROP TABLE IF EXISTS public.DUMMY_TABLE")
        }
    }
}
