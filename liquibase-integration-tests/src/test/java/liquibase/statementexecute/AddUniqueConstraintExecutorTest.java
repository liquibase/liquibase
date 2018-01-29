package liquibase.statementexecute;

import liquibase.change.ColumnConfig;
import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.datatype.DataTypeFactory;
import liquibase.statement.ColumnConstraint;
import liquibase.statement.NotNullConstraint;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.AddUniqueConstraintStatement;
import liquibase.statement.core.CreateTableStatement;
import liquibase.test.DatabaseTestContext;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class AddUniqueConstraintExecutorTest extends AbstractExecuteTest {

    protected static final String TABLE_NAME = "AddUQTest";
    protected static final String COLUMN_NAME = "colToMakeUQ";
    protected static final String CONSTRAINT_NAME = "UQ_TEST";
    protected static final String TABLESPACE_NAME = "LIQUIBASE2";

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
        statementUnderTest = new AddUniqueConstraintStatement(
                DatabaseTestContext.ALT_CATALOG,
                DatabaseTestContext.ALT_SCHEMA,
                TABLE_NAME,
                new ColumnConfig[]
                        {new ColumnConfig().setName(COLUMN_NAME)},
                CONSTRAINT_NAME
        );

        assertCorrect("ALTER TABLE liquibasec.adduqtest ADD CONSTRAINT uq_test UNIQUE (coltomakeuq)", MySQLDatabase
                .class);
        /*
         * In Informix, this test case is actually impossible. While it is allowed to cross-select data from
          * different databases (using the database:schema.table notation), it is not allowed to send DDL to a
          * different database (even if the database is on the same instance). So, even as the following
          * statement is semantically false, it is syntactically correct.
         */
        assertCorrect("ALTER TABLE liquibasec:liquibaseb.adduqtest ADD CONSTRAINT UNIQUE (coltomakeuq) CONSTRAINT " +
                "uq_test", InformixDatabase.class);

        assertCorrect("alter table liquibasec.adduqtest add constraint uq_test unique (coltomakeuq)", OracleDatabase.class);
        assertCorrect("alter table liquibaseb.\"adduqtest\" add constraint uq_test unique (\"coltomakeuq\")", PostgresDatabase.class);
        assertCorrect("alter table liquibasec.adduqtest add constraint uq_test unique (coltomakeuq)", DerbyDatabase
                .class);
        assertCorrect("alter table [liquibaseb].[adduqtest] add constraint [uq_test] unique ([coltomakeuq])",
                SybaseASADatabase.class, SybaseDatabase.class);
        assertCorrect("alter table [liquibasec].[liquibaseb].[adduqtest] add constraint [uq_test] unique " +
                "([coltomakeuq])", MSSQLDatabase.class);
        assertCorrect("alter table [adduqtest] add constraint [uq_test] unique ([coltomakeuq])", FirebirdDatabase.class);

        assertCorrect("alter table [liquibaseb].[adduqtest] add constraint [uq_test] unique ([coltomakeuq])", HsqlDatabase.class);
        assertCorrectOnRest("alter table [liquibasec].[adduqtest] add constraint [uq_test] unique ([coltomakeuq])");

    }

    @SuppressWarnings("unchecked")
    @Test
    public void execute_withTablespace() throws Exception {
        statementUnderTest = new AddUniqueConstraintStatement(null, null, TABLE_NAME, new ColumnConfig[] { new ColumnConfig().setName(COLUMN_NAME)}, CONSTRAINT_NAME).setTablespace(TABLESPACE_NAME);
        assertCorrect("alter table [adduqtest] add constraint [uq_test] unique ([coltomakeuq])", SybaseASADatabase.class, SybaseDatabase.class);
        assertCorrect("alter table [adduqtest] add constraint [uq_test] unique ([coltomakeuq]) on liquibase2", MSSQLDatabase.class);
        assertCorrect("alter table adduqtest add constraint unique (coltomakeuq) constraint uq_test", InformixDatabase.class);
        assertCorrect("alter table \"adduqtest\" add constraint uq_test unique (\"coltomakeuq\") USING INDEX TABLESPACE " + TABLESPACE_NAME, PostgresDatabase.class);
        assertCorrect("alter table adduqtest add constraint uq_test unique (coltomakeuq)", MySQLDatabase.class);
        assertCorrect("alter table adduqtest add constraint uq_test unique (coltomakeuq)", MariaDBDatabase.class);
        assertCorrect("alter table adduqtest add constraint uq_test unique (coltomakeuq)", DerbyDatabase.class, HsqlDatabase.class, DB2Database.class, H2Database.class, FirebirdDatabase.class);
        assertCorrect("alter table [adduqtest] add constraint [uq_test] unique ([coltomakeuq])", Db2zDatabase.class);
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
        assertCorrect("alter table adduqtest add constraint uq_test unique (coltomakeuq) DEFERRABLE INITIALLY " +
                "DEFERRED DISABLE", OracleDatabase.class);
        assertCorrect("ALTER TABLE \"adduqtest\" ADD CONSTRAINT uq_test unique (\"coltomakeuq\") DEFERRABLE INITIALLY" +
                " DEFERRED DISABLE", PostgresDatabase.class);
        assertCorrect("alter table adduqtest add constraint uq_test unique (coltomakeuq)", DerbyDatabase.class);
        assertCorrect("alter table [adduqtest] add constraint [uq_test] unique ([coltomakeuq])");
    }
}
