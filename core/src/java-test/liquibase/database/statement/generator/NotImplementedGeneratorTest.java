package liquibase.database.statement.generator;

import org.junit.Test;
import static org.junit.Assert.*;
import liquibase.database.statement.AddDefaultValueStatement;
import liquibase.database.OracleDatabase;
import liquibase.exception.StatementNotSupportedOnDatabaseException;

public class NotImplementedGeneratorTest {

    @Test(expected = StatementNotSupportedOnDatabaseException.class)
    public void generateSql() throws Exception {
        new NotImplementedGenerator().generateSql(new AddDefaultValueStatement(null, "table", "name", "N/A"), new OracleDatabase());
    }
    
    @Test
    public void getMessage() {
        NotImplementedGenerator generator = new NotImplementedGenerator();
        try {
            generator.generateSql(new AddDefaultValueStatement(null, "table", "name", "N/A"), new OracleDatabase());
        } catch (Exception e) {
            assertEquals("liquibase.database.sql.AddDefaultValueStatement is not supported on Oracle", e.getMessage());
        }

    }
}
