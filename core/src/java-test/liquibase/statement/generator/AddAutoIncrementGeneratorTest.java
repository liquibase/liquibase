package liquibase.statement.generator;

import liquibase.change.AddAutoIncrementChange;
import liquibase.database.*;
import liquibase.database.structure.Column;
import liquibase.database.structure.DatabaseObject;
import liquibase.database.structure.Schema;
import liquibase.database.structure.Table;
import liquibase.statement.AddAutoIncrementStatement;
import liquibase.statement.CreateTableStatement;
import liquibase.statement.NotNullConstraint;
import liquibase.statement.SqlStatement;
import liquibase.test.TestContext;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AddAutoIncrementGeneratorTest <T extends AddAutoIncrementStatement>
	extends AbstractSqlGeneratorTest<T> {

	protected static final String TABLE_NAME = "table_name";

    public AddAutoIncrementGeneratorTest() throws Exception {
    	this(new AddAutoIncrementGenerator());
    }

    public AddAutoIncrementGeneratorTest(AddAutoIncrementGenerator generatorUnderTest) throws Exception {
        super(generatorUnderTest);
    }

    
	@Override
	protected List<? extends SqlStatement> setupStatements(
			Database database) {
		ArrayList<CreateTableStatement> statements = new ArrayList<CreateTableStatement>();
		CreateTableStatement table = new CreateTableStatement(null, TABLE_NAME);
		if (database instanceof MySQLDatabase) {
			table.addPrimaryKeyColumn("id", "int", null, "pk_");
		} else {
			table.addColumn("id", "int", new NotNullConstraint());
		}
		statements.add(table);
        
		if (database.supportsSchemas()) {
			table = new CreateTableStatement(TestContext.ALT_SCHEMA, TABLE_NAME);
			table
				.addColumn("id", "int", new NotNullConstraint());
			statements.add(table);
		}
		return statements;
	}



    @SuppressWarnings("unchecked")
	protected T createSampleSqlStatement() {
        return (T) new AddAutoIncrementStatement(null, null, null, null);
    }

    @Override
	protected boolean waitForException(Database database) {
		return database instanceof MSSQLDatabase;
	}

	@SuppressWarnings("unchecked")
	@Test
    public void generateSql_noSchema() throws Exception {
        AddAutoIncrementStatement statement = new AddAutoIncrementStatement(null, "table_name", "id", "int");
        testSqlOnAllExcept("ALTER TABLE [table_name] MODIFY [id] int AUTO_INCREMENT", (T) statement
        		, PostgresDatabase.class
        		, MySQLDatabase.class
        );
        testSqlOn("alter table [table_name] modify id serial auto_increment", (T) statement, PostgresDatabase.class);
        testSqlOn("alter table `table_name` modify `id` int auto_increment", (T) statement, MySQLDatabase.class);
    }


    @Override
    protected boolean shouldBeImplementation(Database database) {
        return database.supportsAutoIncrement() 
        && !(database instanceof DerbyDatabase) 
        && !(database instanceof MSSQLDatabase) 
        && !(database instanceof HsqlDatabase);
    }

    @Test
    public void getAffectedDatabaseObjects() throws Exception {
        for (Database database : TestContext.getInstance().getAvailableDatabases()) {
            AddAutoIncrementChange change = new AddAutoIncrementChange();
            change.setSchemaName("SCHEMA_NAME");
            change.setTableName("TABLE_NAME");
            change.setColumnName("COLUMN_NAME");
            change.setColumnDataType("INT");

            Set<DatabaseObject> affectedDatabaseObjects = change.getAffectedDatabaseObjects(database);
            if (affectedDatabaseObjects.size() > 0) {
                assertEquals(3, affectedDatabaseObjects.size());
            }

            for (DatabaseObject databaseObject : affectedDatabaseObjects) {
                if (databaseObject instanceof Schema) {
                    assertEquals("SCHEMA_NAME", ((Schema) databaseObject).getName());
                } else if (databaseObject instanceof Table) {
                        assertEquals("SCHEMA_NAME", ((Table) databaseObject).getSchema());
                        assertEquals("TABLE_NAME", ((Table) databaseObject).getName());
                } else {
                    assertEquals("COLUMN_NAME", ((Column) databaseObject).getName());
                    assertEquals("TABLE_NAME", ((Column) databaseObject).getTable().getName());
                }
            }
        }
    }

//      @Test
//    public void execute_stringDefault() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(null, new AddColumnStatement(null, TABLE_NAME, NEW_COLUMN_NAME, "varchar(50)", "new default")) {
//                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
//                        assertNull(snapshot.getTable(TABLE_NAME).getColumn(NEW_COLUMN_NAME));
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
//                        Column columnSnapshot = snapshot.getTable(TABLE_NAME).getColumn(NEW_COLUMN_NAME);
//                        assertNotNull(columnSnapshot);
//                        assertEquals(NEW_COLUMN_NAME.toUpperCase(), columnSnapshot.getName().toUpperCase());
//                        assertEquals("varchar".toUpperCase(), columnSnapshot.getTypeName().toUpperCase().replaceAll("VARCHAR2", "VARCHAR"));
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
//                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
//                        assertNull(snapshot.getTable(TABLE_NAME).getColumn(NEW_COLUMN_NAME));
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
//                        Column columnSnapshot = snapshot.getTable(TABLE_NAME).getColumn(NEW_COLUMN_NAME);
//                        assertNotNull(columnSnapshot);
//                        assertEquals(NEW_COLUMN_NAME.toUpperCase(), columnSnapshot.getName().toUpperCase());
//                        if (snapshot.getDatabase() instanceof OracleDatabase) {
//                            assertEquals("NUMBER", columnSnapshot.getTypeName().toUpperCase());
//                        } else {
//                            assertTrue(columnSnapshot.getTypeName().toUpperCase().startsWith("INT"));
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
//                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
//                        assertNull(snapshot.getTable(TABLE_NAME).getColumn(NEW_COLUMN_NAME));
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
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
//                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
//                        assertNull(snapshot.getTable(TABLE_NAME).getColumn(NEW_COLUMN_NAME));
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
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
//                    protected boolean expectedException(Database database, JDBCException exception) {
//                        return (database instanceof DB2Database
//                                || database instanceof DerbyDatabase
//                                || database instanceof H2Database
//                                || database instanceof CacheDatabase);
//                    }
//
//                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
//                        assertNull(snapshot.getTable(TABLE_NAME).getColumn(NEW_COLUMN_NAME));
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
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
//                    protected void preExecuteAssert(DatabaseSnapshot snapshot) {
//                        assertNull(snapshot.getTable(TABLE_NAME).getColumn(NEW_COLUMN_NAME));
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshot snapshot) {
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
//                      protected boolean expectedException(Database database, JDBCException exception) {
//                          return (database instanceof DB2Database
//                                  || database instanceof DerbyDatabase
//                                  || database instanceof H2Database
//                                  || database instanceof CacheDatabase
//                                    || !database.supportsAutoIncrement());
//                      }
//
//                      protected void preExecuteAssert(DatabaseSnapshot snapshot) {
//                          assertNull(snapshot.getTable(TABLE_NAME).getColumn(NEW_COLUMN_NAME));
//                      }
//
//                      protected void postExecuteAssert(DatabaseSnapshot snapshot) {
//                          Column columnSnapshot = snapshot.getTable(TABLE_NAME).getColumn(NEW_COLUMN_NAME);
//                          assertNotNull(columnSnapshot);
//                          assertEquals(false, columnSnapshot.isNullable());
//                          assertTrue(columnSnapshot.isPrimaryKey());
//                          assertEquals(true, columnSnapshot.isAutoIncrement());
//                      }
//                  });
//      }
}
