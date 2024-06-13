package liquibase.snapshot

import liquibase.Scope
import liquibase.database.AbstractJdbcDatabase
import liquibase.database.Database
import liquibase.database.jvm.JdbcConnection
import liquibase.exception.DatabaseException
import liquibase.extension.testing.testsystem.DatabaseTestSystem
import liquibase.extension.testing.testsystem.TestSystemFactory
import liquibase.extension.testing.testsystem.spock.LiquibaseIntegrationTest
import liquibase.logging.core.BufferedLogService
import liquibase.parser.ChangeLogParserConfiguration
import spock.lang.Shared
import spock.lang.Specification

import java.sql.Connection
import java.util.logging.Level

@LiquibaseIntegrationTest
class JdbcDatabaseConnectionCloseTest extends Specification {
    @Shared
    public DatabaseTestSystem h2 = Scope.currentScope.getSingleton(TestSystemFactory).getTestSystem("h2")

    def "Validate connection thrown during close is handled correctly"() {
        when:
        JdbcConnection connection = new TestJdbcConnection(h2.getConnection())
        Database database = h2.getDatabaseFromFactory()
        database.setConnection(connection)
        database.close()

        then:
        thrown(DatabaseException)
    }

    def "Test Exception thrown during auto-commit"() {
        when:
        BufferedLogService bufferLog = new BufferedLogService()

        Scope.child(Scope.Attr.logService.name(), bufferLog, () -> {
            JdbcConnection connection = new TestJdbcConnectionAutoCommit(h2.getConnection())
            Database database = h2.getDatabaseFromFactory()
            ((AbstractJdbcDatabase)database).setPreviousAutoCommit(false)
            database.setConnection(connection)
            database.close()
        })

        then:
        def dbe = thrown(DatabaseException)
        assert dbe
        assert bufferLog.getLogAsString(Level.WARNING).contains("WARNING Failed to restore the auto commit to false")
    }

    private class TestJdbcConnection extends JdbcConnection {
        TestJdbcConnection(Connection sqlConnection) {
            super(sqlConnection)
        }

        @Override
        void close() throws DatabaseException {
            throw new DatabaseException("This exception is for test")
        }
    }

    private class TestJdbcConnectionAutoCommit extends JdbcConnection {
        TestJdbcConnectionAutoCommit(Connection sqlConnection) {
            super(sqlConnection)
        }

        @Override
        void setAutoCommit(boolean autoCommit) throws DatabaseException {
            throw new DatabaseException("This exception is for test")
        }
    }

}
