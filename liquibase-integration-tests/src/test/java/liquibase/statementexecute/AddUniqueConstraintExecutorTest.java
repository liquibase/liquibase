package liquibase.statementexecute;

import liquibase.change.ColumnConfig;
import liquibase.database.*;
import liquibase.database.core.*;
import liquibase.datatype.DataTypeFactory;
import liquibase.statement.ColumnConstraint;
import liquibase.test.DatabaseTestContext;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.CreateTableStatement;
import liquibase.statement.NotNullConstraint;
import liquibase.statement.core.AddUniqueConstraintStatement;

import java.util.List;
import java.util.ArrayList;

import org.junit.Test;

public class AddUniqueConstraintExecutorTest extends AbstractExecuteTest {

    protected static final String TABLE_NAME = "AddUQTest";
    protected static final String COLUMN_NAME = "colToMakeUQ";
    protected static final String CONSTRAINT_NAME = "UQ_TEST";
    protected static final String TABLESPACE_NAME = "LB_TABLESPACE";

    @Override
    protected List<? extends SqlStatement> setupStatements(Database database) {
        List<CreateTableStatement> statements = new ArrayList<CreateTableStatement>();
        CreateTableStatement table = new CreateTableStatement(null, null, TABLE_NAME);
        table
                .addColumn("id", DataTypeFactory.getInstance().fromDescription("int", database), null, new ColumnConstraint[]{ new NotNullConstraint()})
                .addColumn(COLUMN_NAME, DataTypeFactory.getInstance().fromDescription("int", database), null, new ColumnConstraint[]{ new NotNullConstraint()});
        statements.add(table);

        if (database.supportsSchemas()) {
            table = new CreateTableStatement(DatabaseTestContext.ALT_CATALOG, DatabaseTestContext.ALT_SCHEMA, TABLE_NAME);
            table
                    .addColumn("id", DataTypeFactory.getInstance().fromDescription("int", database), null, new ColumnConstraint[]{  new NotNullConstraint() })
                    .addColumn(COLUMN_NAME, DataTypeFactory.getInstance().fromDescription("int", database), null,  new ColumnConstraint[]{ new NotNullConstraint() });
            statements.add(table);
        }
        return statements;
    }

    //    @Test
//    public void execute_noSchema() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(null, new AddUniqueConstraintStatement(null, TABLE_NAME, COLUMN_NAME, "uq_adduqtest")) {
//
//                    protected void preExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertFalse(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).isUnique());
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        //todo: enable snapshot and assertion when snapshot can check for unique constraints
//                        //snapshot = new DatabaseSnapshotGenerator(snapshot);
//                    	assertTrue(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).isUnique());
//                    }
//                });
//    }
//
//    @Test
//    public void execute_withSchema() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(TestContext.ALT_SCHEMA, new AddUniqueConstraintStatement(TestContext.ALT_SCHEMA, TABLE_NAME, COLUMN_NAME, "uq_adduqtest")) {
//                    protected void preExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertFalse(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).isUnique());
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        //todo: enable snapshot and assertion when snapshot can check for unique constraints
//                snapshot = new DatabaseSnapshotGenerator(database, TestContext.ALT_SCHEMA);
//                assertTrue(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).isUnique());
//                    }
//
//                });
//    }
//
//    @Test
//    public void execute_withTablespace() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(null, new AddUniqueConstraintStatement(null, TABLE_NAME, COLUMN_NAME, "uq_adduqtest").setTablespace(TestContext.ALT_TABLESPACE)) {
//                    protected void preExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertFalse(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).isUnique());
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        //todo: enable snapshot and assertion when snapshot can check for unique constraints
//                        // snapshot = new DatabaseSnapshotGenerator(database);
////                assertTrue(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).isUnique());
//                    }
//                });
//    }

    @SuppressWarnings("unchecked")
    @Test
    public void execute_noSchema() throws Exception {
        this.statementUnderTest = new AddUniqueConstraintStatement(null, null, TABLE_NAME, new ColumnConfig[] { new ColumnConfig().setName(COLUMN_NAME)}, CONSTRAINT_NAME);
        assertCorrect("alter table [adduqtest] add constraint [uq_test] unique ([coltomakeuq])", SybaseDatabase.class);
        assertCorrect("alter table [adduqtest] add constraint [uq_test] unique ([coltomakeuq])", MSSQLDatabase.class);
        assertCorrect("alter table [adduqtest] add constraint [uq_test] unique ([coltomakeuq])", SybaseASADatabase.class);
        assertCorrect("alter table adduqtest add constraint uq_test unique (coltomakeuq)", MySQLDatabase.class);
        assertCorrect("alter table adduqtest add constraint unique (coltomakeuq) constraint uq_test", InformixDatabase.class);
        assertCorrect("alter table adduqtest add constraint uq_test unique (coltomakeuq)", OracleDatabase.class);
        assertCorrect("alter table \"adduqtest\" add constraint uq_test unique (\"coltomakeuq\")", PostgresDatabase.class);
        assertCorrect("alter table adduqtest add constraint uq_test unique (coltomakeuq)", DerbyDatabase.class);
        assertCorrect("alter table [adduqtest] add constraint [uq_test] unique ([coltomakeuq])");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void execute_noConstraintName() throws Exception {
        this.statementUnderTest = new AddUniqueConstraintStatement(null, null, TABLE_NAME, new ColumnConfig[] { new ColumnConfig().setName(COLUMN_NAME)}, null);
		assertCorrect("alter table adduqtest add unique (coltomakeuq)", MySQLDatabase.class);
		assertCorrect("alter table adduqtest add constraint unique (coltomakeuq)", InformixDatabase.class);
		assertCorrect("alter table adduqtest add unique (coltomakeuq)", OracleDatabase.class);
		assertCorrect("alter table \"adduqtest\" add unique (\"coltomakeuq\")", PostgresDatabase.class);
		assertCorrect("alter table adduqtest add unique (coltomakeuq)", DerbyDatabase.class);
        assertCorrect("alter table [adduqtest] add unique ([coltomakeuq])", SybaseASADatabase.class, SybaseDatabase.class);
        assertCorrect("alter table [adduqtest] add unique ([coltomakeuq])", MSSQLDatabase.class);

		assertCorrect("alter table [adduqtest] add unique ([coltomakeuq])");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void execute_withSchema() throws Exception {
        statementUnderTest = new AddUniqueConstraintStatement(DatabaseTestContext.ALT_CATALOG, DatabaseTestContext.ALT_SCHEMA, TABLE_NAME, new ColumnConfig[] { new ColumnConfig().setName(COLUMN_NAME)}, CONSTRAINT_NAME);

        // FIXME Syntax for mysql is correct, but exception "Table 'liquibaseb.adduqtest' doesn't exist" is thrown
// 		assertCorrect("alter table `liquibaseb`.`adduqtest` add constraint `uq_test` unique (`coltomakeuq`)", MySQLDatabase.class);
        assertCorrect("alter table liquibaseb.adduqtest add constraint unique (coltomakeuq) constraint uq_test", InformixDatabase.class);
        assertCorrect("alter table liquibasec.adduqtest add constraint uq_test unique (coltomakeuq)", OracleDatabase.class);
        assertCorrect("alter table liquibaseb.\"adduqtest\" add constraint uq_test unique (\"coltomakeuq\")", PostgresDatabase.class);
        assertCorrect("alter table liquibasec.adduqtest add constraint uq_test unique (coltomakeuq)", DerbyDatabase.class);
        assertCorrect("alter table [liquibaseb].[adduqtest] add constraint [uq_test] unique ([coltomakeuq])", SybaseASADatabase.class, SybaseDatabase.class, MSSQLDatabase.class);
        assertCorrect("alter table [adduqtest] add constraint [uq_test] unique ([coltomakeuq])", FirebirdDatabase.class);

        assertCorrect("alter table [liquibaseb].[adduqtest] add constraint [uq_test] unique ([coltomakeuq])", HsqlDatabase.class);
        assertCorrectOnRest("alter table [liquibasec].[adduqtest] add constraint [uq_test] unique ([coltomakeuq])");

    }

    @SuppressWarnings("unchecked")
	@Test
	public void execute_withTablespace() throws Exception {
		statementUnderTest = new AddUniqueConstraintStatement(null, null, TABLE_NAME, new ColumnConfig[] { new ColumnConfig().setName(COLUMN_NAME)}, CONSTRAINT_NAME).setTablespace(TABLESPACE_NAME);
        assertCorrect("alter table [adduqtest] add constraint [uq_test] unique ([coltomakeuq])", SybaseASADatabase.class, SybaseDatabase.class);
        assertCorrect("alter table [adduqtest] add constraint [uq_test] unique ([coltomakeuq])", MSSQLDatabase.class);
        assertCorrect("alter table adduqtest add constraint unique (coltomakeuq) constraint uq_test", InformixDatabase.class);
        assertCorrect("alter table \"adduqtest\" add constraint uq_test unique (\"coltomakeuq\") USING INDEX TABLESPACE " + TABLESPACE_NAME, PostgresDatabase.class);
        assertCorrect("alter table adduqtest add constraint uq_test unique (coltomakeuq)", MySQLDatabase.class);
        assertCorrect("alter table adduqtest add constraint uq_test unique (coltomakeuq)", DerbyDatabase.class, HsqlDatabase.class, DB2Database.class, H2Database.class, FirebirdDatabase.class);
		assertCorrectOnRest("alter table [adduqtest] add constraint [uq_test] unique ([coltomakeuq]) USING INDEX TABLESPACE " + TABLESPACE_NAME);
	}

    @SuppressWarnings("unchecked")
	@Test
	public void execute_withDefferedAndDisabled() throws Exception {
		statementUnderTest = new AddUniqueConstraintStatement(null, null, TABLE_NAME, new ColumnConfig[] { new ColumnConfig().setName(COLUMN_NAME)}, CONSTRAINT_NAME).setDeferrable(true).setInitiallyDeferred(true).setDisabled(true);
        assertCorrect("alter table [adduqtest] add constraint [uq_test] unique ([coltomakeuq])", SybaseDatabase.class);
        assertCorrect("alter table [adduqtest] add constraint [uq_test] unique ([coltomakeuq])", MSSQLDatabase.class);
        assertCorrect("alter table [adduqtest] add constraint [uq_test] unique ([coltomakeuq])", SybaseASADatabase.class);
        assertCorrect("alter table adduqtest add constraint uq_test unique (coltomakeuq)", MySQLDatabase.class);
        assertCorrect("alter table adduqtest add constraint unique (coltomakeuq) constraint uq_test", InformixDatabase.class);
        assertCorrect("alter table adduqtest add constraint uq_test unique (coltomakeuq) DEFERRABLE INITIALLY DEFERRED DISABLE", OracleDatabase.class);
        assertCorrect("alter table \"adduqtest\" add constraint uq_test unique (\"coltomakeuq\")", PostgresDatabase.class);
        assertCorrect("alter table adduqtest add constraint uq_test unique (coltomakeuq)", DerbyDatabase.class);
        assertCorrect("alter table [adduqtest] add constraint [uq_test] unique ([coltomakeuq])");
	}
}
