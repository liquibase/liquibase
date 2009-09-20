package liquibase.snapshot.core;

import liquibase.database.Database;
import liquibase.database.structure.Column;
import liquibase.database.structure.ForeignKey;
import liquibase.database.structure.Table;
import liquibase.database.core.DB2Database;
import liquibase.database.core.H2Database;
import liquibase.exception.DatabaseException;
import liquibase.executor.ExecutorService;
import liquibase.statement.core.RawSqlStatement;

import java.util.List;
import java.util.Map;

public class H2DatabaseSnapshotGenerator extends JdbcDatabaseSnapshotGenerator {
    public boolean supports(Database database) {
        return database instanceof H2Database;
    }

    public int getPriority(Database database) {
        return PRIORITY_DATABASE;
    }

    @Override
    public Table getTable(String schemaName, String tableName, Database database) throws DatabaseException {
        return super.getTable(schemaName, convertTableNameToDatabaseTableName(tableName), database);
    }

    protected String convertTableNameToDatabaseTableName(String tableName) {
        return tableName.toUpperCase();
    }

    protected String convertColumnNameToDatabaseTableName(String columnName) {
        return columnName.toUpperCase();
    }
}