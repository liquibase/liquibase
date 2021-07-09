package liquibase.executor.jvm;

import liquibase.database.Database;
import liquibase.database.core.PostgresDatabase;

public class JdbcExampleExecutor extends JdbcExecutor {

    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT + 100;
    }

    @Override
    public boolean supports(Database database) {
        return database instanceof JdbcExampleExecutor.ExampleJdbcDatabase;
    }

    public static class ExampleJdbcDatabase extends PostgresDatabase {
    }

}
