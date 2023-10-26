package liquibase.database;

import java.util.Arrays;
import java.util.List;

public class StandardLiquibaseTableNames implements LiquibaseTableNames {
    @Override
    public List<String> getLiquibaseGeneratedTableNames(Database database) {
        return Arrays.asList(database.getDatabaseChangeLogTableName(), database.getDatabaseChangeLogLockTableName());
    }
}
