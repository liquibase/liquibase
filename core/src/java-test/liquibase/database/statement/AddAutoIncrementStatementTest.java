package liquibase.database.statement;

public class AddAutoIncrementStatementTest { // extends AbstractSqStatementTest {

    private static final String TABLE_NAME = "AddAutoIncTest";
    private static final String COLUMN_NAME = "testCol";
    private static final String COLUMN_TYPE = "int";

//    @Test
//    public void execute_defaultSchema() throws Exception {
//        for (Database database : TestContext.getInstance().getAvailableDatabases()) {
//            DatabaseConnection conn = database.getConnection();
//            Statement statement = conn.createStatement();
//            AddAutoIncrementGenerator generator = new AddAutoIncrementGenerator();
//            for (Sql sql : generator.generateSql(new AddAutoIncrementStatement(null, TABLE_NAME, COLUMN_NAME, COLUMN_TYPE), new MySQLDatabase())) {
//                System.out.println(sql.toSql());
//                statement.execute(sql.toSql());
//            }
//        }
//    }

//
//    protected void setupDatabase(Database database) throws Exception {
//        dropAndCreateTable(new CreateTableStatement(null, TABLE_NAME)
//                .addPrimaryKeyColumn(COLUMN_NAME, COLUMN_TYPE, null, null)
//                .addColumn("otherColumn", "varchar(50)"), database);
//
//        dropAndCreateTable(new CreateTableStatement(TestContext.ALT_SCHEMA, TABLE_NAME)
//                .addPrimaryKeyColumn(COLUMN_NAME, COLUMN_TYPE, null, null)
//                .addColumn("otherColumn", "varchar(50)"), database);
//    }
//
//    protected AddAutoIncrementGenerator createGeneratorUnderTest() {
//        return new AddAutoIncrementGenerator();
//    }
//
//    @Test
//    public void isValidGenerator() throws Exception {
//        new DatabaseTestTemplate().testOnAllDatabases(new DatabaseTest() {
//            public void performTest(Database database) throws Exception {
//                if (database instanceof OracleDatabase
//                        || database instanceof MSSQLDatabase
//                        || database instanceof PostgresDatabase
//                        || database instanceof DerbyDatabase
//                        || database instanceof CacheDatabase
//                        || database instanceof H2Database
//                        || database instanceof FirebirdDatabase
//                        || database instanceof SybaseASADatabase
//                ) {
//                    assertFalse(createGeneratorUnderTest().isValidGenerator(null, database));
//                } else {
//                    assertTrue(createGeneratorUnderTest().isValidGenerator(null, database));
//                }
//            }
//
//        });
//    }
//
//    @Test
//    public void execute_defaultSchema() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(null, new AddAutoIncrementStatement(null, TABLE_NAME, COLUMN_NAME, COLUMN_TYPE)) {
//
//                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
//                        assertFalse(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).isAutoIncrement());
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
//                        assertTrue(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).isAutoIncrement());
//                    }
//                });
//    }
//
//    @Test
//    public void execute_alternateSchema() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(TestContext.ALT_SCHEMA, new AddAutoIncrementStatement(TestContext.ALT_SCHEMA, TABLE_NAME, COLUMN_NAME, COLUMN_TYPE)) {
//
//                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
//                        assertFalse(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).isAutoIncrement());
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
//                        assertTrue(snapshot.getTable(TABLE_NAME).getColumn(COLUMN_NAME).isAutoIncrement());
//                    }
//                });
//    }
}
