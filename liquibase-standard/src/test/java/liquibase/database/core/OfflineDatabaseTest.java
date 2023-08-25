package liquibase.database.core;

import liquibase.change.AddColumnConfig;
import liquibase.change.core.AddColumnChange;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
import liquibase.database.OfflineConnection;
import liquibase.exception.DatabaseException;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.AddColumnStatement;
import liquibase.test.JUnitResourceAccessor;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Fail.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OfflineDatabaseTest {

    private Database createOfflineDatabase(String url) throws Exception {
        DatabaseConnection databaseConnection = new OfflineConnection(url, new JUnitResourceAccessor());
        return DatabaseFactory.getInstance().findCorrectDatabaseImplementation(databaseConnection);
    }

    /**
     * Check if it it's possible to output SQL from an OfflineConnection
     * set to Oracle (offline:oracle).
     *
     * @see <a href="https://liquibase.jira.com/browse/CORE-2192">CORE-2192</a>
     */
    @Test
    public void canOutputSQLFromOfflineOracleDB() throws Exception {
        AddColumnChange change = new AddColumnChange();
        AddColumnConfig column1 = new AddColumnConfig();
        column1.setName("column1");
        column1.setType("INT");
        change.addColumn(column1);
        AddColumnConfig column2 = new AddColumnConfig();
        column2.setName("column2");
        column2.setType("INT");
        change.addColumn(column2);

        SqlStatement[] statements = new SqlStatement[0];
        try {
            statements = change.generateStatements(createOfflineDatabase("offline:oracle"));
        } catch (DatabaseException e) {
            fail("Can't generate statements from an Offline Oracle database.");
        }
        assertEquals(1, statements.length);
        assertTrue(statements[0] instanceof AddColumnStatement);
        AddColumnStatement stmt = (AddColumnStatement) statements[0];
        assertTrue(stmt.isMultiple());
        assertEquals(2, stmt.getColumns().size());
    }
}
