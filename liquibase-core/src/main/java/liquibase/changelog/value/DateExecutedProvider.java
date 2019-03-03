package liquibase.changelog.value;

import liquibase.database.Database;
import liquibase.statement.DatabaseFunction;
import liquibase.statement.core.MarkChangeSetRanStatement;

public class DateExecutedProvider implements ChangeLogColumnValueProvider {
    @Override
    public Object getValue(MarkChangeSetRanStatement statement, Database database) {
        String dateValue = database.getCurrentDateTimeFunction();
        return new DatabaseFunction(dateValue);
    }
}
