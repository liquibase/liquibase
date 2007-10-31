package liquibase.database.sql;

import liquibase.database.Database;
import liquibase.database.template.JdbcTemplate;
import liquibase.exception.JDBCException;
import liquibase.test.DatabaseTest;
import liquibase.test.DatabaseTestTemplate;
import static org.junit.Assert.*;
import org.junit.Test;

import java.sql.SQLException;

public abstract class AbstractSqlStatementTest {

    protected void dropAndCreateTable(CreateTableStatement statement, Database database) throws SQLException, JDBCException {
        dropTableIfExists(statement.getSchemaName(), statement.getSchemaName(), database);

        new JdbcTemplate(database).execute(statement);

        if (!database.getAutoCommitMode()) {
            database.getConnection().commit();
        }

    }

    protected void dropTableIfExists(String schemaName, String tableName, Database database) throws SQLException {
        if (!database.getAutoCommitMode()) {
            database.getConnection().commit();
        }

        String schema = "";
        if (schemaName != null) {
            schema = schemaName + ".";
        }

        try {
            new JdbcTemplate(database).execute(new RawSqlStatement("drop table " + schema + tableName));

            if (!database.getAutoCommitMode()) {
                database.getConnection().commit();
            }
            
        } catch (JDBCException e) {
            if (!database.getConnection().getAutoCommit()) {
                database.getConnection().rollback();
            }
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
