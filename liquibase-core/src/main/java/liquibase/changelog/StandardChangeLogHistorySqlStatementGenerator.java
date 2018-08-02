package liquibase.changelog;

import liquibase.changelog.definition.ChangeLogTableChangesProvider;
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
        for(ChangeLogTableChangesProvider alterTableStatementProvider : tableDefinition.getUpdateTableSqlStatementProviders()) {
            statementsToExecute.addAll(alterTableStatementProvider.createSqlStatements(database, changeLogTable));
        }
        return statementsToExecute;
    }

}
