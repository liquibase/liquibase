package liquibase.changelog.value;

import liquibase.changelog.ChangeLogHistoryServiceFactory;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import liquibase.statement.core.MarkChangeSetRanStatement;

public class OrderExecutedProvider implements ChangeLogColumnValueProvider {
    @Override
    public Object getValue(MarkChangeSetRanStatement statement, Database database) throws LiquibaseException {
        return ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(database).getNextSequenceValue();
    }
}
