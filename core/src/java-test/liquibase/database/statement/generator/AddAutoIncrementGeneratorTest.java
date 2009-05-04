package liquibase.database.statement.generator;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import liquibase.change.AddAutoIncrementChange;
import liquibase.database.Database;
import liquibase.database.DerbyDatabase;
import liquibase.database.HsqlDatabase;
import liquibase.database.MSSQLDatabase;
import liquibase.database.MySQLDatabase;
import liquibase.database.PostgresDatabase;
import liquibase.database.statement.AddAutoIncrementStatement;
import liquibase.database.statement.CreateTableStatement;
import liquibase.database.statement.NotNullConstraint;
import liquibase.database.statement.SqlStatement;
import liquibase.database.structure.Column;
import liquibase.database.structure.DatabaseObject;
import liquibase.database.structure.Schema;
import liquibase.database.structure.Table;
import liquibase.test.TestContext;

import org.junit.Test;

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



    protected T createSampleSqlStatement() {
        return (T) new AddAutoIncrementStatement(null, null, null, null);
    }

    @SuppressWarnings("unchecked")
	@Test
    public void generateSql_noSchema() throws Exception {
        AddAutoIncrementStatement statement = new AddAutoIncrementStatement(null, "table_name", "id", "int");
        testSqlOnAllExcept("ALTER TABLE [table_name] MODIFY [id] int AUTO_INCREMENT", (T) statement, PostgresDatabase.class
                // TODO sqlserver does not allow change autoincrement property for field :( 
        		, MSSQLDatabase.class
        		, MySQLDatabase.class
        		);
        testSqlOn("alter table [table_name] modify id serial auto_increment", (T) statement, PostgresDatabase.class);
        testSqlOn("alter table `table_name` modify `id` int auto_increment", (T) statement, MySQLDatabase.class);
    }


    @Override
    protected boolean shouldBeImplementation(Database database) {
        return database.supportsAutoIncrement() && !(database instanceof DerbyDatabase) && !(database instanceof HsqlDatabase);
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

}
