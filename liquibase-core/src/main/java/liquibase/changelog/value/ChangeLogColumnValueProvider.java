package liquibase.changelog.value;

import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import liquibase.statement.core.MarkChangeSetRanStatement;

public interface ChangeLogColumnValueProvider {

    Object getValue(MarkChangeSetRanStatement statement, Database database) throws LiquibaseException;

}
