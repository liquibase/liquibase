package liquibase.statement;


import liquibase.change.ColumnConfig;
import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.resource.ResourceAccessor;

import java.util.List;

/**
 * Handles INSERT Execution
 */
public class InsertExecutablePreparedStatement extends ExecutablePreparedStatementBase {

    public InsertExecutablePreparedStatement(
            Database database, String catalogName, String schemaName, String tableName, List<? extends ColumnConfig> columns,
            ChangeSet changeSet, ResourceAccessor resourceAccessor) {
        super(database, catalogName, schemaName, tableName, columns, changeSet, resourceAccessor);
    }

    @Override
    public boolean continueOnError() {
        return false;
    }

}
