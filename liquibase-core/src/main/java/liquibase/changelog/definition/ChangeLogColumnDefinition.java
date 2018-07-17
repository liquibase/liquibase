package liquibase.changelog.definition;

import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.statement.SqlStatement;
import liquibase.structure.core.Table;

import java.util.List;

public interface ChangeLogColumnDefinition {

    List<SqlStatement> complementChangeLogTable(Database database, Table changeLogTable) throws DatabaseException;

}
