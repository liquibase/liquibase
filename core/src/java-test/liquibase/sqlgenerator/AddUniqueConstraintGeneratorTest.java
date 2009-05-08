package liquibase.sqlgenerator;

import liquibase.database.*;
import liquibase.statement.AddUniqueConstraintStatement;
import liquibase.statement.CreateTableStatement;
import liquibase.statement.NotNullConstraint;
import liquibase.statement.SqlStatement;
import liquibase.test.TestContext;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class AddUniqueConstraintGeneratorTest extends AbstractSqlGeneratorTest<AddUniqueConstraintStatement> {
    protected static final String TABLE_NAME = "AddUQTest";
    protected static final String COLUMN_NAME = "colToMakeUQ";
    protected static final String CONSTRAINT_NAME = "UQ_TEST";

    public AddUniqueConstraintGeneratorTest() throws Exception {
        this(new AddUniqueConstraintGenerator());
    }

    public AddUniqueConstraintGeneratorTest(SqlGenerator<AddUniqueConstraintStatement> generatorUnderTest) throws Exception {
        super(generatorUnderTest);
    }

//    protected void setupDatabase(Database database) throws Exception {
//            dropAndCreateTable(new CreateTableStatement(null, TABLE_NAME)
//                    .addColumn("id", "int", new NotNullConstraint())
//                    .addColumn(COLUMN_NAME, "int", new NotNullConstraint()), database);
//
//            dropAndCreateTable(new CreateTableStatement(TestContext.ALT_SCHEMA, TABLE_NAME)
//                    .addColumn("id", "int", new NotNullConstraint())
//                    .addColumn(COLUMN_NAME, "int", new NotNullConstraint()), database);
//    }
//
//    protected SqlStatement createGeneratorUnderTest() {
//        return new AddUniqueConstraintStatement(null, null, null, null);
//    }

//    @Test
//    public void execute_noSchema() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(null, new AddUniqueConstraintStatement(null, TABLE_NAME, COLUMN_NAME, "uq_adduqtest")) {
//
//                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
//                        assertFalse(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).isUnique());
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
//                        //todo: enable snapshot and assertion when snapshot can check for unique constraints
//                        //snapshot = new DatabaseSnapshot(snapshot);
//                    	assertTrue(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).isUnique());
//                    }
//                });
//    }
//
//    @Test
//    public void execute_withSchema() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(TestContext.ALT_SCHEMA, new AddUniqueConstraintStatement(TestContext.ALT_SCHEMA, TABLE_NAME, COLUMN_NAME, "uq_adduqtest")) {
//                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
//                        assertFalse(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).isUnique());
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
//                        //todo: enable snapshot and assertion when snapshot can check for unique constraints
//                snapshot = new DatabaseSnapshot(database, TestContext.ALT_SCHEMA);
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
//                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
//                        assertFalse(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).isUnique());
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
//                        //todo: enable snapshot and assertion when snapshot can check for unique constraints
//                        // snapshot = new DatabaseSnapshot(database);
////                assertTrue(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).isUnique());
//                    }
//                });
//    }

    @Override
    protected AddUniqueConstraintStatement createSampleSqlStatement() {
        return new AddUniqueConstraintStatement(null, null, null, null);
    }

	@Override
    protected List<? extends SqlStatement> setupStatements(Database database) {
		List<CreateTableStatement> statements = new ArrayList<CreateTableStatement>();
		CreateTableStatement table = new CreateTableStatement(null, TABLE_NAME);
		table
			.addColumn("id", "int", new NotNullConstraint())
	        .addColumn(COLUMN_NAME, "int", new NotNullConstraint());
		statements.add(table);
        
		if (database.supportsSchemas()) {
			table = new CreateTableStatement(TestContext.ALT_SCHEMA, TABLE_NAME);
			table
				.addColumn("id", "int", new NotNullConstraint())
		        .addColumn(COLUMN_NAME, "int", new NotNullConstraint());
			statements.add(table);
		}
		return statements;
	}


    @Override
    protected boolean shouldBeImplementation(Database database) {
        return !(database instanceof SQLiteDatabase)
                && !(database instanceof MSSQLDatabase)
                && !(database instanceof SybaseDatabase)
                && !(database instanceof SybaseASADatabase)
                ;
    }

    @SuppressWarnings("unchecked")
	@Test
    public void execute_noSchema() throws Exception {
    	AddUniqueConstraintStatement statement = new AddUniqueConstraintStatement(null, TABLE_NAME, COLUMN_NAME, CONSTRAINT_NAME);
        testSqlOnAllExcept("alter table [adduqtest] add constraint [uq_test] unique ([coltomakeuq])", statement
                , MySQLDatabase.class, InformixDatabase.class, OracleDatabase.class, PostgresDatabase.class, DerbyDatabase.class);
        testSqlOn("alter table `adduqtest` add constraint `uq_test` unique (`coltomakeuq`)", statement, MySQLDatabase.class);
        testSqlOn("alter table adduqtest add constraint unique (coltomakeuq) constraint uq_test", statement, InformixDatabase.class);
        testSqlOn("alter table adduqtest add constraint uq_test unique (coltomakeuq)", statement, OracleDatabase.class);
        testSqlOn("alter table \"adduqtest\" add constraint uq_test unique (\"coltomakeuq\")", statement, PostgresDatabase.class);
        testSqlOn("alter table adduqtest add constraint uq_test unique (coltomakeuq)", statement, DerbyDatabase.class);
    }

    @Test
    public void execute_noConstraintName() throws Exception {
    	AddUniqueConstraintStatement statement = new AddUniqueConstraintStatement(null, TABLE_NAME, COLUMN_NAME, null);
//		testSqlOnAllExcept("alter table [adduqtest] add constraint [uq_test]", statement
//				, MySQLDatabase.class, InformixDatabase.class, OracleDatabase.class, PostgresDatabase.class, DerbyDatabase.class);
//		testSqlOn("alter table `adduqtest` add constraint `null` unique (`coltomakeuq`)", statement, MySQLDatabase.class);
//		testSqlOn("alter table adduqtest add constraint unique (coltomakeuq) constraint uq_test", statement, InformixDatabase.class);
//		testSqlOn("alter table adduqtest add constraint uq_test unique (coltomakeuq)", statement, OracleDatabase.class);
//		testSqlOn("alter table \"adduqtest\" add constraint uq_test unique (\"coltomakeuq\")", statement, PostgresDatabase.class);
//		testSqlOn("alter table adduqtest add constraint uq_test unique (coltomakeuq)", statement, DerbyDatabase.class);
    }

    @Test
    public void execute_withSchema() throws Exception {
    	AddUniqueConstraintStatement statement = new AddUniqueConstraintStatement(TestContext.ALT_SCHEMA, TABLE_NAME, COLUMN_NAME, CONSTRAINT_NAME);

        testSqlOnAllExcept("alter table liquibaseb.[adduqtest] add constraint [uq_test] unique ([coltomakeuq])", statement
                , MySQLDatabase.class, InformixDatabase.class, OracleDatabase.class, PostgresDatabase.class, DerbyDatabase.class
                // FIXME seems like FirebirdDatabase does not support schema attribute. Check it!
                , FirebirdDatabase.class
        );

        // FIXME Syntax for mysql is correct, but exception "Table 'liquibaseb.adduqtest' doesn't exist" is thrown
// 		testSqlOn("alter table `liquibaseb`.`adduqtest` add constraint `uq_test` unique (`coltomakeuq`)", statement, MySQLDatabase.class);
        testSqlOn("alter table liquibaseb.adduqtest add constraint unique (coltomakeuq) constraint uq_test", statement, InformixDatabase.class);
        testSqlOn("alter table liquibaseb.adduqtest add constraint uq_test unique (coltomakeuq)", statement, OracleDatabase.class);
        testSqlOn("alter table liquibaseb.\"adduqtest\" add constraint uq_test unique (\"coltomakeuq\")", statement, PostgresDatabase.class);
        testSqlOn("alter table liquibaseb.adduqtest add constraint uq_test unique (coltomakeuq)", statement, DerbyDatabase.class);
    }

//    @Test
//    public void execute_noSchema() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(null, new AddUniqueConstraintStatement(null, TABLE_NAME, COLUMN_NAME, "uq_adduqtest")) {
//
//                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
//                        assertFalse(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).isUnique());
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
//                        //todo: enable snapshot and assertion when snapshot can check for unique constraints
//                        // snapshot = new DatabaseSnapshot(database);
////                assertTrue(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).isUnique());
//                    }
//                });
//    }
//
//    @Test
//    public void execute_withSchema() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(TestContext.ALT_SCHEMA, new AddUniqueConstraintStatement(TestContext.ALT_SCHEMA, TABLE_NAME, COLUMN_NAME, "uq_adduqtest")) {
//                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
//                        assertFalse(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).isUnique());
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
//                        //todo: enable snapshot and assertion when snapshot can check for unique constraints
////                snapshot = new DatabaseSnapshot(database, TestContext.ALT_SCHEMA);
////                assertTrue(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).isUnique());
//                    }
//
//                });
//    }
//
//    @Test
//    public void execute_withTablespace() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(null, new AddUniqueConstraintStatement(null, TABLE_NAME, COLUMN_NAME, "uq_adduqtest").setTablespace(TestContext.ALT_TABLESPACE)) {
//                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
//                        assertFalse(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).isUnique());
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
//                        //todo: enable snapshot and assertion when snapshot can check for unique constraints
//                        // snapshot = new DatabaseSnapshot(database);
////                assertTrue(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).isUnique());
//                    }
//                });
//    }

}
