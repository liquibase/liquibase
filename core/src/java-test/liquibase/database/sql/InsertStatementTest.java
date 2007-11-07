package liquibase.database.sql;

import liquibase.database.Database;
import liquibase.database.FirebirdDatabase;
import liquibase.database.structure.DatabaseSnapshot;
import liquibase.database.template.JdbcTemplate;
import liquibase.test.DatabaseTestTemplate;
import liquibase.test.SqlStatementDatabaseTest;
import liquibase.test.TestContext;
import static org.junit.Assert.*;
import org.junit.Test;

public class InsertStatementTest extends AbstractSqlStatementTest {
    private static final String TABLE_NAME = "InsertTest";
    private static final String VARCHAR_COL_NAME = "colName_vc";
    private static final String DATE_COL_NAME = "colName_dt";
    private static final String BOOLEAN_COL_NAME = "colName_b";
    private static final String INT_COL_NAME = "colName_i";
    private static final String FLOAT_COL_NAME = "colName_f";

    protected void setupDatabase(Database database) throws Exception {
        dropAndCreateTable(new CreateTableStatement(TABLE_NAME)
                .addColumn(VARCHAR_COL_NAME, "varchar(50)")
                .addColumn(DATE_COL_NAME, "varchar(50)")
                .addColumn(BOOLEAN_COL_NAME, database.getBooleanType())
                .addColumn(INT_COL_NAME, "int")
                .addColumn(FLOAT_COL_NAME, "float")
                , database);

        dropAndCreateTable(new CreateTableStatement(TestContext.ALT_SCHEMA, TABLE_NAME)
                .addColumn(VARCHAR_COL_NAME, "varchar(50)")
                .addColumn(DATE_COL_NAME, "varchar(50)")
                .addColumn(BOOLEAN_COL_NAME, database.getBooleanType())
                .addColumn(INT_COL_NAME, "int")
                .addColumn(FLOAT_COL_NAME, "float")
                , database);
    }

    protected SqlStatement generateTestStatement() {
        return new InsertStatement(null, null);
    }

    @Test
    public void execute_defaultSchema() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(
                new SqlStatementDatabaseTest(null,
                        new InsertStatement(null, TABLE_NAME)
                                .addColumnValue(VARCHAR_COL_NAME, "new value")) {

                    private int oldCount;

                    protected void preExecuteAssert(DatabaseSnapshot snapshot) throws Exception {
                        oldCount = new JdbcTemplate(snapshot.getDatabase()).queryForInt(new RawSqlStatement("select count(*) from " + TABLE_NAME));
                    }

                    protected void postExecuteAssert(DatabaseSnapshot snapshot) throws Exception {
                        int newCount = new JdbcTemplate(snapshot.getDatabase()).queryForInt(new RawSqlStatement("select count(*) from " + TABLE_NAME));
                        assertEquals(oldCount + 1, newCount);
                    }

                });
    }

    @Test
    public void execute_altSchema() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(
                new SqlStatementDatabaseTest(TestContext.ALT_SCHEMA,
                        new InsertStatement(TestContext.ALT_SCHEMA, TABLE_NAME)
                                .addColumnValue(VARCHAR_COL_NAME, "new value")) {

                    private int oldCount;

                    protected boolean supportsTest(Database database) {
                        return !(database instanceof FirebirdDatabase);
                    }

                    protected void preExecuteAssert(DatabaseSnapshot snapshot) throws Exception {
                        oldCount = new JdbcTemplate(snapshot.getDatabase()).queryForInt(new RawSqlStatement("select count(*) from " + TestContext.ALT_SCHEMA + "." + TABLE_NAME));
                    }

                    protected void postExecuteAssert(DatabaseSnapshot snapshot) throws Exception {
                        int newCount = new JdbcTemplate(snapshot.getDatabase()).queryForInt(new RawSqlStatement("select count(*) from " + TestContext.ALT_SCHEMA + "." + TABLE_NAME));
                        assertEquals(oldCount + 1, newCount);
                    }

                });
    }

    @Test
    public void execute_multiColumns() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(
                new SqlStatementDatabaseTest(null,
                        new InsertStatement(null, TABLE_NAME)
                                .addColumnValue(VARCHAR_COL_NAME, "new value")
                                .addColumnValue(DATE_COL_NAME, new java.sql.Date(new java.util.Date().getTime()))
                                .addColumnValue(INT_COL_NAME, 42)
                                .addColumnValue(FLOAT_COL_NAME, 123.456)) {

                    private int oldCount;

                    protected void preExecuteAssert(DatabaseSnapshot snapshot) throws Exception {
                        oldCount = new JdbcTemplate(snapshot.getDatabase()).queryForInt(new RawSqlStatement("select count(*) from " + TABLE_NAME));
                    }

                    protected void postExecuteAssert(DatabaseSnapshot snapshot) throws Exception {
                        int newCount = new JdbcTemplate(snapshot.getDatabase()).queryForInt(new RawSqlStatement("select count(*) from " + TABLE_NAME));
                        assertEquals(oldCount + 1, newCount);
                    }

                });
    }
}
