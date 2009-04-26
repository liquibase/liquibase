package liquibase.database.statement.generator;

import liquibase.database.statement.AddAutoIncrementStatement;
import liquibase.database.statement.CreateTableStatement;
import liquibase.database.statement.PrimaryKeyConstraint;
import liquibase.database.statement.SqlStatement;
import liquibase.database.*;
import org.junit.Test;

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
        
        testSqlOnAllExcept("ALTER TABLE [table_name] MODIFY [id] int AUTO_INCREMENT", statement, MySQLDatabase.class, PostgresDatabase.class);
        testSqlOn("ALTER TABLE [table_name] MODIFY [id] int PRIMARY KEY AUTO_INCREMENT", statement, MySQLDatabase.class);
        testSqlOn("alter table [table_name] modify id serial auto_increment", statement, PostgresDatabase.class);
    }


    @Override
    protected boolean shouldBeImplementation(Database database) {
        return database.supportsAutoIncrement() && !(database instanceof DerbyDatabase) && !(database instanceof HsqlDatabase);
    }
}
