package liquibase.database.statement;

import liquibase.database.Database;
import liquibase.database.PostgresDatabase;
import liquibase.database.statement.generator.SqlGenerator;
import liquibase.database.template.Executor;
import liquibase.exception.JDBCException;
import org.junit.Before;

import java.sql.SQLException;

public abstract class AbstractSqStatementTest<GeneratorUnderTest extends SqlGenerator> {

    @Before
    public void setupAvailableDatabases() throws Exception {
//        for (Database database : TestContext.getInstance().getAvailableDatabases()) {
//            if (createGeneratorUnderTest().isValidGenerator(null, database)) {
//                if (database.supportsSchemas()) {
//                    database.dropDatabaseObjects(TestContext.ALT_SCHEMA);
//                }
//                database.dropDatabaseObjects(null);
//                setupDatabase(database);
//            }
//        }
    }

    protected abstract void setupDatabase(Database database) throws Exception;

    protected void dropAndCreateTable(CreateTableStatement statement, Database database) throws SQLException, JDBCException {
        if (statement.getSchemaName() != null && !database.supportsSchemas()) {
            return;
        }

        dropTableIfExists(statement.getSchemaName(), statement.getTableName(), database);

        new Executor(database).execute(statement);

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
            new Executor(database).execute(new DropTableStatement(schemaName, tableName, cascade));

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

        new Executor(database).execute(statement);

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
            new Executor(database).execute(new DropSequenceStatement(schemaName, sequenceName));

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

        new Executor(database).execute(statement);

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
            new Executor(database).execute(new RawSqlStatement("drop view " + schema + viewName));

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

    protected abstract SqlStatement createGeneratorUnderTest();

//    @Test
//    public void isValidGenerator() throws Exception {
//        new DatabaseTestTemplate().testOnAllDatabases(new DatabaseTest() {
//            public void performTest(Database database) throws Exception {
//                assertTrue(createGeneratorUnderTest().isValidGenerator(null, database));
//            }
//        });
//    }
}
