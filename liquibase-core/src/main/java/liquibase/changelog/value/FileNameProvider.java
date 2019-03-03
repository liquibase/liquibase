package liquibase.changelog.value;

import liquibase.database.Database;
import liquibase.statement.core.MarkChangeSetRanStatement;

public class FileNameProvider implements ChangeLogColumnValueProvider {
    @Override
    public Object getValue(MarkChangeSetRanStatement statement, Database database) {
        return statement.getChangeSet().getFilePath();
    }
}
