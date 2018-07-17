package liquibase.changelog;

import liquibase.changelog.definition.ChangeLogColumnDefinition;
import liquibase.changelog.definition.ChangeLogTableDefinition;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.statement.SqlStatement;
import liquibase.structure.core.Table;

import java.util.ArrayList;
import java.util.List;

public class StandardChangeLogHistorySqlStatementGenerator {

    List<SqlStatement> changeLogTableUpdate(Database database, Table changeLogTable, ChangeLogTableDefinition tableDefinition) throws DatabaseException {
        List<SqlStatement> statementsToExecute = new ArrayList<>();
        for(ChangeLogColumnDefinition columnDefinition : tableDefinition.getColumnDefinitions()) {
            statementsToExecute.addAll(columnDefinition.complementChangeLogTable(database, changeLogTable));
        }
        return statementsToExecute;
    }

}
