package liquibase.database;

import liquibase.Scope;
import liquibase.changelog.ChangeLogHistoryServiceFactory;
import liquibase.exception.DatabaseException;
import liquibase.lockservice.LockServiceFactory;

import java.util.Arrays;
import java.util.List;

public class StandardLiquibaseTableNames implements LiquibaseTableNames {
    @Override
    public List<String> getLiquibaseGeneratedTableNames(Database database) {
        return Arrays.asList(database.getDatabaseChangeLogTableName(), database.getDatabaseChangeLogLockTableName());
    }

    @Override
    public void destroy(Database database) throws DatabaseException {
        Scope.getCurrentScope().getSingleton(ChangeLogHistoryServiceFactory.class).getChangeLogService(database).destroy();
        LockServiceFactory.getInstance().getLockService(database).destroy();
    }
}
