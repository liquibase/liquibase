package liquibase.changelog.value;

import liquibase.database.Database;
import liquibase.statement.core.MarkChangeSetRanStatement;

public class ExecTypeProvider implements ChangeLogColumnValueProvider {
    @Override
    public Object getValue(MarkChangeSetRanStatement statement, Database database) {
        return statement.getExecType().value;
    }
}
