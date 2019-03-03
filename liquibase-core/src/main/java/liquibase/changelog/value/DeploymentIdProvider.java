package liquibase.changelog.value;

import liquibase.changelog.ChangeLogHistoryServiceFactory;
import liquibase.database.Database;
import liquibase.statement.core.MarkChangeSetRanStatement;

public class DeploymentIdProvider implements ChangeLogColumnValueProvider {
    @Override
    public Object getValue(MarkChangeSetRanStatement statement, Database database) {
       return ChangeLogHistoryServiceFactory.getInstance()
               .getChangeLogService(database)
               .getDeploymentId();
    }
}
