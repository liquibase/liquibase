package liquibase.database.sql;

import liquibase.database.Database;
import liquibase.database.PostgresDatabase;
import liquibase.database.template.JdbcTemplate;
import liquibase.exception.JDBCException;
import liquibase.test.DatabaseTest;
import liquibase.test.DatabaseTestTemplate;
import liquibase.test.TestContext;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;

public abstract class AbstractSqlStatementTest {

    @Before
    public void setupAvailableDatabases() throws Exception {
        for (Database database : TestContext.getInstance().getAvailableDatabases()) {
            if (generateTestStatement().supportsDatabase(database)) {
                if (database.supportsSchemas()) {
                    database.dropDatabaseObjects(TestContext.ALT_SCHEMA);
                }
                database.dropDatabaseObjects(null);
                setupDatabase(database);
            }
        }
    }

    protected abstract void setupDatabase(Database database) throws Exception;

    protected void dropAndCreateTable(CreateTableStatement statement, Database database) throws SQLException, JDBCException {
        if (statement.getSchemaName() != null && !database.supportsSchemas()) {
            return;
        }

        dropTableIfExists(statement.getSchemaName(), statement.getTableName(), database);

        new JdbcTemplate(database).execute(statement);

        if (!database.getAutoCommitMode()) {
            database.getConnection().commit();
        }

    }

    protected void dropTableIfExists(String schemaName, String tableName, Database database) throws SQLException {
        if (schemaName != null && !database.supportsSchemas()) {
            return;
        }

        if (!database.getAutoCommitMode()) {
            database.getConnection().commit();
        }

        try {
            boolean cascade = false;
            if (database instanceof PostgresDatabase) {
                cascade = true;
            }
            new JdbcTemplate(database).execute(new DropTableStatement(schemaName, tableName, cascade));

            if (!database.getAutoCommitMode()) {
                database.getConnection().commit();
            }

        } catch (JDBCException e) {
//            System.out.println("Error dropping table "+database.escapeTableName(schemaName, tableName)+" on "+database);
//            e.printStackTrace();
            if (!database.getConnection().getAutoCommit()) {
                database.getConnection().rollback();
            }
        }
    }

    protected void dropAndCreateSequence(CreateSequenceStatement statement, Database database) throws SQLException, JDBCException {
        if (statement.getSchemaName() != null && !database.supportsSchemas()) {
            return;
        }

        dropSequenceIfExists(statement.getSchemaName(), statement.getSequenceName(), database);

        new JdbcTemplate(database).execute(statement);

        if (!database.getAutoCommitMode()) {
            database.getConnection().commit();
        }

    }

    protected void dropSequenceIfExists(String schemaName, String sequenceName, Database database) throws SQLException {
        if (schemaName != null && !database.supportsSchemas()) {
            return;
        }
        
        if (!database.getAutoCommitMode()) {
            database.getConnection().commit();
        }

        try {
            new JdbcTemplate(database).execute(new DropSequenceStatement(schemaName, sequenceName));

            if (!database.getAutoCommitMode()) {
                database.getConnection().commit();
            }

        } catch (JDBCException e) {
//            System.out.println("Error dropping sequence "+database.escapeSequenceName(schemaName, sequenceName));
//            e.printStackTrace();
            if (!database.getConnection().getAutoCommit()) {
                database.getConnection().rollback();
            }
        }
        if (!database.getAutoCommitMode()) {
            database.getConnection().commit();
        }


    }


    protected void dropAndCreateView(CreateViewStatement statement, Database database) throws SQLException, JDBCException {
        if (statement.getSchemaName() != null && !database.supportsSchemas()) {
            return;
        }

        dropViewIfExists(statement.getSchemaName(), statement.getViewName(), database);

        new JdbcTemplate(database).execute(statement);

        if (!database.getAutoCommitMode()) {
            database.getConnection().commit();
        }

    }

    protected void dropViewIfExists(String schemaName, String viewName, Database database) throws SQLException {
        if (schemaName != null && !database.supportsSchemas()) {
            return;
        }

        if (!database.getAutoCommitMode()) {
            database.getConnection().commit();
        }

        String schema = "";
        if (schemaName != null) {
            schema = schemaName + ".";
        }

        try {
            new JdbcTemplate(database).execute(new RawSqlStatement("drop view " + schema + viewName));

            if (!database.getAutoCommitMode()) {
                database.getConnection().commit();
            }

        } catch (JDBCException e) {
//            System.out.println("Cannot drop view "+database.escapeViewName(schemaName, viewName)+" on "+database);
//            e.printStackTrace();
            if (!database.getConnection().getAutoCommit()) {
                database.getConnection().rollback();
            }
        }

        if (!database.getAutoCommitMode()) {
            database.getConnection().commit();
        }
        
    }

    protected abstract SqlStatement generateTestStatement();

    @Test
    public void supportsDatabase() throws Exception {
        new DatabaseTestTemplate().testOnAllDatabases(new DatabaseTest() {
            public void performTest(Database database) throws Exception {
                assertTrue(generateTestStatement().supportsDatabase(database));
            }
        });
    }

    @Test
    public void getEndDelimiter() throws Exception {
        new DatabaseTestTemplate().testOnAllDatabases(new DatabaseTest() {
            public void performTest(Database database) throws Exception {
                assertEquals(";", generateTestStatement().getEndDelimiter(database));
            }
        });
    }
}
