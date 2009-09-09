package liquibase.snapshot.core;

import liquibase.database.Database;
import liquibase.database.structure.Column;
import liquibase.database.structure.ForeignKey;
import liquibase.database.core.DB2Database;
import liquibase.database.core.H2Database;
import liquibase.database.core.HsqlDatabase;
import liquibase.exception.DatabaseException;
import liquibase.executor.ExecutorService;
import liquibase.statement.core.RawSqlStatement;

import java.util.List;
import java.util.Map;

public class HsqlDatabaseSnapshotGenerator extends JdbcDatabaseSnapshotGenerator {
    public boolean supports(Database database) {
        return database instanceof HsqlDatabase;
    }

    public int getPriority(Database database) {
        return PRIORITY_DATABASE;
    }

    @Override
    public Column getColumn(String schemaName, String tableName, String columnName, Database database) throws DatabaseException {
        return super.getColumn(schemaName, tableName.toUpperCase(), columnName.toUpperCase(),database);
    }

   @Override
    public List<ForeignKey> getForeignKeys(String schemaName, String tableName, Database database) throws DatabaseException {
        return super.getForeignKeys(schemaName, tableName.toUpperCase(), database);
    }
}