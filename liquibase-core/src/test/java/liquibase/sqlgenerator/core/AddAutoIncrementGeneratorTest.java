package liquibase.sqlgenerator.core;

import static org.junit.Assert.assertEquals;

import java.math.BigInteger;

import liquibase.database.Database;
import liquibase.database.core.DerbyDatabase;
import liquibase.database.core.HsqlDatabase;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.H2Database;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.AbstractSqlGeneratorTest;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.statement.core.AddAutoIncrementStatement;

public class AddAutoIncrementGeneratorTest extends AbstractSqlGeneratorTest<AddAutoIncrementStatement> {

	protected static final String TABLE_NAME = "TABLE_NAME";
	protected static final String COLUMN_NAME = "COLUMN_NAME";
	protected static final String SCHEMA_NAME = "SCHEMA_NAME";
    protected static final String CATALOG_NAME = "CATALOG_NAME";

    public AddAutoIncrementGeneratorTest() throws Exception {
    	this(new AddAutoIncrementGenerator());
    }

    protected AddAutoIncrementGeneratorTest(SqlGenerator<AddAutoIncrementStatement> addAutoIncrementGenerator) throws Exception {
        super(addAutoIncrementGenerator);
    }

    @Override
	protected AddAutoIncrementStatement createSampleSqlStatement() {
        return new AddAutoIncrementStatement(null, null, TABLE_NAME, COLUMN_NAME, null, null, null);
    }

    @Override
	protected boolean waitForException(Database database) {
		return database instanceof MSSQLDatabase;
	}


    @Override
    protected boolean shouldBeImplementation(Database database) {
        return database.supportsAutoIncrement() 
        && !(database instanceof DerbyDatabase)
        && !(database instanceof MSSQLDatabase) 
        && !(database instanceof HsqlDatabase)
        && !(database instanceof H2Database);
    }

    protected AddAutoIncrementStatement createAddAutoIncrementStatement(
    		Database database, BigInteger startWith, BigInteger incrementBy) {
		
    	AddAutoIncrementStatement statement = new AddAutoIncrementStatement(
                CATALOG_NAME,
        	SCHEMA_NAME,
        	TABLE_NAME,
        	COLUMN_NAME,
        	"BIGINT",
        	startWith,
        	incrementBy);   	
    	
    	return statement;
    }
    
    protected void testAddAutoIncrementStatement(
    		Database database, BigInteger startWith, BigInteger incrementBy, String expectedSql)
    		throws Exception {
		AddAutoIncrementStatement statement = createAddAutoIncrementStatement(
			database, startWith, incrementBy);
		
		Sql[] generatedSql = this.generatorUnderTest.generateSql(statement, database, null);
		
		assertEquals(expectedSql, generatedSql[0].toSql());
    }
    
//    @Test
//    public void getAffectedDatabaseObjects() throws Exception {
//        for (Database database : DatabaseTestContext.getInstance().getAvailableDatabases()) {
//            AddAutoIncrementChange change = new AddAutoIncrementChange();
//            change.setSchemaName("SCHEMA_NAME");
//            change.setTableName("TABLE_NAME");
//            change.setColumnName("COLUMN_NAME");
//            change.setColumnDataType("INT");
//
//            Set<DatabaseObject> affectedDatabaseObjects = change.getAffectedDatabaseObjects(database);
//            if (affectedDatabaseObjects.size() > 0) {
//                assertEquals(3, affectedDatabaseObjects.size());
//            }
//
//            for (DatabaseObject databaseObject : affectedDatabaseObjects) {
//                if (databaseObject instanceof Schema) {
//                    assertEquals("SCHEMA_NAME", ((Schema) databaseObject).getName());
//                } else if (databaseObject instanceof Table) {
//                        assertEquals("SCHEMA_NAME", ((Table) databaseObject).getSchema());
//                        assertEquals("TABLE_NAME", ((Table) databaseObject).getName());
//                } else {
//                    assertEquals("COLUMN_NAME", ((Column) databaseObject).getName());
//                    assertEquals("TABLE_NAME", ((Column) databaseObject).getTable().getName());
//                }
//            }
//        }
//    }

//      @Test
//    public void execute_stringDefault() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(null, new AddColumnStatement(null, TABLE_NAME, NEW_COLUMN_NAME, "varchar(50)", "new default")) {
//                    protected void preExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertNull(snapshot.getTable(TABLE_NAME).getColumn(NEW_COLUMN_NAME));
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        Column columnSnapshot = snapshot.getTable(TABLE_NAME).getColumn(NEW_COLUMN_NAME);
//                        assertNotNull(columnSnapshot);
//                        assertEquals(NEW_COLUMN_NAME.toUpperCase(), columnSnapshot.getName().toUpperCase());
//                        assertEquals("varchar".toUpperCase(), columnSnapshot.getShortName().toUpperCase().replaceAll("VARCHAR2", "VARCHAR"));
//                        assertEquals(50, columnSnapshot.getColumnSize());
//                        assertEquals("new default", columnSnapshot.getDefaultValue());
//
//                        assertEquals(true, columnSnapshot.isNullable());
//                    }
//                });
//    }
//
//    @Test
//    public void execute_intDefault() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(null, new AddColumnStatement(null, TABLE_NAME, NEW_COLUMN_NAME, "int", 42)) {
//                    protected void preExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertNull(snapshot.getTable(TABLE_NAME).getColumn(NEW_COLUMN_NAME));
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        Column columnSnapshot = snapshot.getTable(TABLE_NAME).getColumn(NEW_COLUMN_NAME);
//                        assertNotNull(columnSnapshot);
//                        assertEquals(NEW_COLUMN_NAME.toUpperCase(), columnSnapshot.getName().toUpperCase());
//                        if (snapshot.getDatabase() instanceof OracleDatabase) {
//                            assertEquals("NUMBER", columnSnapshot.getShortName().toUpperCase());
//                        } else {
//                            assertTrue(columnSnapshot.getShortName().toUpperCase().startsWith("INT"));
//                        }
//                        assertEquals(42, ((Number) columnSnapshot.getDefaultValue()).intValue());
//
//                        assertEquals(true, columnSnapshot.isNullable());
//                    }
//
//                }
//
//        );
//    }
//
//    @Test
//    public void execute_floatDefault() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(null, new AddColumnStatement(null, TABLE_NAME, NEW_COLUMN_NAME, "float", 42.5)) {
//                    protected void preExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertNull(snapshot.getTable(TABLE_NAME).getColumn(NEW_COLUMN_NAME));
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        Column columnSnapshot = snapshot.getTable(TABLE_NAME).getColumn(NEW_COLUMN_NAME);
//                        assertNotNull(columnSnapshot);
//                        assertEquals(NEW_COLUMN_NAME.toUpperCase(), columnSnapshot.getName().toUpperCase());
//                        assertEquals(new Double(42.5), new Double(((Number) columnSnapshot.getDefaultValue()).doubleValue()));
//
//                        assertEquals(true, columnSnapshot.isNullable());
//                    }
//                });
//    }
//
//    @Test
//    public void execute_notNull() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(null, new AddColumnStatement(null, TABLE_NAME, NEW_COLUMN_NAME, "int", 42, new NotNullConstraint())) {
//                    protected void preExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertNull(snapshot.getTable(TABLE_NAME).getColumn(NEW_COLUMN_NAME));
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        Column columnSnapshot = snapshot.getTable(TABLE_NAME).getColumn(NEW_COLUMN_NAME);
//                        assertNotNull(columnSnapshot);
//                        assertEquals(false, columnSnapshot.isNullable());
//                    }
//                }
//
//        );
//    }
//
//    @Test
//    public void execute_primaryKey_nonAutoIncrement() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(null, new AddColumnStatement(null, TABLE_NAME, NEW_COLUMN_NAME, "int", null, new PrimaryKeyConstraint())) {
//
//                    protected boolean expectedException(Database database, DatabaseException exception) {
//                        return (database instanceof DB2Database
//                                || database instanceof DerbyDatabase
//                                || database instanceof H2Database
//                                || database instanceof CacheDatabase);
//                    }
//
//                    protected void preExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertNull(snapshot.getTable(TABLE_NAME).getColumn(NEW_COLUMN_NAME));
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        Column columnSnapshot = snapshot.getTable(TABLE_NAME).getColumn(NEW_COLUMN_NAME);
//                        assertNotNull(columnSnapshot);
//                        assertEquals(false, columnSnapshot.isNullable());
//                        assertTrue(columnSnapshot.isPrimaryKey());
//                        assertEquals(false, columnSnapshot.isAutoIncrement());
//                    }
//                });
//    }
//
//    @Test
//    public void execute_altSchema() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(TestContext.ALT_SCHEMA, new AddColumnStatement(TestContext.ALT_SCHEMA, TABLE_NAME, NEW_COLUMN_NAME, "varchar(50)", "new default")) {
//                    protected void preExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertNull(snapshot.getTable(TABLE_NAME).getColumn(NEW_COLUMN_NAME));
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        Column columnSnapshot = snapshot.getTable(TABLE_NAME).getColumn(NEW_COLUMN_NAME);
//                        assertNotNull(columnSnapshot);
//                        assertEquals(NEW_COLUMN_NAME.toUpperCase(), columnSnapshot.getName().toUpperCase());
//                        assertEquals("new default", columnSnapshot.getDefaultValue());
//
//                        assertEquals(true, columnSnapshot.isNullable());
//                    }
//
//                });
//    }
//
//    @Test
//      public void execute_primaryKeyAutoIncrement() throws Exception {
//          new DatabaseTestTemplate().testOnAvailableDatabases(
//                  new SqlStatementDatabaseTest(null, new AddColumnStatement(null, TABLE_NAME, NEW_COLUMN_NAME, "int", null, new PrimaryKeyConstraint(), new AutoIncrementConstraint())) {
//
//                      protected boolean expectedException(Database database, DatabaseException exception) {
//                          return (database instanceof DB2Database
//                                  || database instanceof DerbyDatabase
//                                  || database instanceof H2Database
//                                  || database instanceof CacheDatabase
//                                    || !database.supportsAutoIncrement());
//                      }
//
//                      protected void preExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                          assertNull(snapshot.getTable(TABLE_NAME).getColumn(NEW_COLUMN_NAME));
//                      }
//
//                      protected void postExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                          Column columnSnapshot = snapshot.getTable(TABLE_NAME).getColumn(NEW_COLUMN_NAME);
//                          assertNotNull(columnSnapshot);
//                          assertEquals(false, columnSnapshot.isNullable());
//                          assertTrue(columnSnapshot.isPrimaryKey());
//                          assertEquals(true, columnSnapshot.isAutoIncrement());
//                      }
//                  });
//      }


}
