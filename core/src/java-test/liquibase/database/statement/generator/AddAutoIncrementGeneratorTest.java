package liquibase.database.statement.generator;

import liquibase.database.statement.AddAutoIncrementStatement;
import liquibase.database.statement.CreateTableStatement;
import liquibase.database.statement.PrimaryKeyConstraint;
import liquibase.database.statement.SqlStatement;
import liquibase.database.*;
import liquibase.database.structure.DatabaseObject;
import liquibase.database.structure.Table;
import liquibase.database.structure.Column;
import liquibase.database.structure.Schema;
import liquibase.change.AddAutoIncrementChange;
import liquibase.test.TestContext;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

import java.util.Set;

public class AddAutoIncrementGeneratorTest extends AbstractSqlGeneratorTest {

    public AddAutoIncrementGeneratorTest() {
        this(new AddAutoIncrementGenerator());
    }

    public AddAutoIncrementGeneratorTest(SqlGenerator generatorUnderTest) {
        super(generatorUnderTest);
    }

    protected SqlStatement[] setupStatements() {
        return new SqlStatement[]{
                new CreateTableStatement(null, "table_name")
                        .addColumn("id", "int", new PrimaryKeyConstraint())
        };
    }

    protected AddAutoIncrementStatement createSampleSqlStatement() {
        return new AddAutoIncrementStatement(null, null, null, null);
    }

    @Test
    public void generateSql_noSchema() throws Exception {
        AddAutoIncrementStatement statement = new AddAutoIncrementStatement(null, "table_name", "id", "int");
        
        testSqlOnAllExcept("ALTER TABLE [table_name] MODIFY [id] int AUTO_INCREMENT", statement, PostgresDatabase.class);
        testSqlOn("alter table [table_name] modify id serial auto_increment", statement, PostgresDatabase.class);
    }


    @Override
    protected boolean shouldBeImplementation(Database database) {
        return database.supportsAutoIncrement() && !(database instanceof DerbyDatabase);
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
