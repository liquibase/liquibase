package liquibase.statement;

import liquibase.change.ColumnConfig;
import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.resource.ResourceAccessor;

import java.util.List;

public class ExecutablePreparedStatementBaseMock extends ExecutablePreparedStatementBase {

    public static final String generateSql = "generateSql";

    protected ExecutablePreparedStatementBaseMock(Database database, String catalogName, String schemaName, String
            tableName, List<ColumnConfig> columns, ChangeSet changeSet, ResourceAccessor resourceAccessor) {
        super(database, catalogName, schemaName, tableName, columns, changeSet, resourceAccessor);
    }

    @Override
    protected String generateSql(List<ColumnConfig> cols) {
        return generateSql;
    }

    @Override
    public boolean continueOnError() {
        return false;
    }
}
