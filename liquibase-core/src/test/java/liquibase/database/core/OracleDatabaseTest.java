package liquibase.database.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;
import liquibase.database.AbstractJdbcDatabaseTest;
import liquibase.database.Database;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.exception.LiquibaseException;
import liquibase.executor.ExecutorService;
import liquibase.sdk.executor.MockExecutor;
import liquibase.sql.visitor.SqlVisitor;
import liquibase.statement.SequenceNextValueFunction;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.UpdateStatement;

/**
 * Tests for {@link liquibase.database.core.OracleDatabase}.
 */
public class OracleDatabaseTest extends AbstractJdbcDatabaseTest {

    public OracleDatabaseTest() throws Exception {
        super(new OracleDatabase());
    }

    @Override
    protected String getProductNameString() {
        return "Oracle";
    }

     @Override
     @Test
    public void escapeTableName_noSchema() {
        Database database = getDatabase();
        assertEquals("tableName", database.escapeTableName(null, null, "tableName"));
    }

    @Override
    @Test
    public void escapeTableName_withSchema() {
        Database database = getDatabase();
        assertEquals("catalogName.tableName", database.escapeTableName("catalogName", "schemaName", "tableName"));
    }

    @Override
    @Test
    public void supportsInitiallyDeferrableColumns() {
        assertTrue(getDatabase().supportsInitiallyDeferrableColumns());
    }


    @Override
    @Test
    public void getCurrentDateTimeFunction() {
        Assert.assertEquals("SYSTIMESTAMP", getDatabase().getCurrentDateTimeFunction());
    }

    public void testGetDefaultDriver() {
        Database database = new OracleDatabase();

        assertEquals("oracle.jdbc.OracleDriver", database.getDefaultDriver("jdbc:oracle:thin:@localhost/XE"));

        assertNull(database.getDefaultDriver("jdbc:db2://localhost;databaseName=liquibase"));
    }

    @Test
    public void validateCore2953WrongSqlOnValueSequenceNext() throws LiquibaseException {
        Database database = getDatabase();
        database.setObjectQuotingStrategy(ObjectQuotingStrategy.QUOTE_ALL_OBJECTS);
        database.setDefaultSchemaName("sampleschema");

        MockExecutor mockExecutor = new MockExecutor();
        mockExecutor.setDatabase(database);

        ExecutorService.getInstance().setExecutor(database, mockExecutor);

        UpdateStatement updateStatement = new UpdateStatement(null, null, "test_table");
        updateStatement.addNewColumnValue("id", new SequenceNextValueFunction("test_table_id_seq"));

        database.execute(new SqlStatement[]{updateStatement}, new ArrayList<SqlVisitor>());

        assertEquals("UPDATE \"SAMPLESCHEMA\".\"test_table\" SET \"id\" = \"SAMPLESCHEMA\".\"test_table_id_seq\".nextval;", mockExecutor.getRanSql().trim());
    }
}

