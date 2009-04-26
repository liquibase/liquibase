package liquibase.database.statement.generator;

import org.junit.Test;
import liquibase.database.statement.AddAutoIncrementStatement;
import liquibase.database.Database;
import liquibase.database.DB2Database;

public class AddAutoIncrementGeneratorDB2Test extends AddAutoIncrementGeneratorTest {

    public AddAutoIncrementGeneratorDB2Test() {
        super(new AddAutoIncrementGeneratorDB2());
    }

    @Test
    public void generateSql_noSchema() throws Exception {
        testSqlOnAll("ALTER TABLE [table_name] ALTER COLUMN [column_name] SET GENERATED ALWAYS AS IDENTITY", new AddAutoIncrementStatement(null, "table_name", "column_name", "int"));
    }

     @Override
    protected boolean shouldBeImplementation(Database database) {
        return database instanceof DB2Database;
    }
}