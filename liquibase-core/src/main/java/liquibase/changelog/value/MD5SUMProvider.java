package liquibase.changelog.value;

import liquibase.database.Database;
import liquibase.statement.core.MarkChangeSetRanStatement;

public class MD5SUMProvider implements ChangeLogColumnValueProvider {
    @Override
    public Object getValue(MarkChangeSetRanStatement statement, Database database) {
        return statement.getChangeSet().generateCheckSum().toString();
    }
}
