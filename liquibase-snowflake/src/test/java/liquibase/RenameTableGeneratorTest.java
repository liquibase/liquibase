package liquibase;

import liquibase.database.core.SnowflakeDatabase;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.core.RenameTableStatement;
import org.junit.jupiter.api.Test;

public class RenameTableGeneratorTest {
    
    @Test
    public void testWhichGeneratorIsUsed() {
        SnowflakeDatabase database = new SnowflakeDatabase();
        RenameTableStatement statement = new RenameTableStatement(null, null, "old_table", "new_table");
        
        SqlGeneratorFactory factory = SqlGeneratorFactory.getInstance();
        Sql[] sqls = factory.generateSql(statement, database);
        
        System.out.println("Generated SQL count: " + sqls.length);
        for (Sql sql : sqls) {
            System.out.println("Generated SQL: " + sql.toSql());
        }
    }
}