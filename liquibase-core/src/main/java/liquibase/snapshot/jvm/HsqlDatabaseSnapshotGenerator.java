package liquibase.snapshot.jvm;

import liquibase.database.Database;
import liquibase.database.core.HsqlDatabase;

public class HsqlDatabaseSnapshotGenerator extends JdbcDatabaseSnapshotGenerator {
    public boolean supports(Database database) {
        return database instanceof HsqlDatabase;
    }

    public int getPriority(Database database) {
        return PRIORITY_DATABASE;
    }

    @Override
    protected String convertTableNameToDatabaseTableName(String tableName) {
        return tableName.toUpperCase();
    }

    @Override
    protected String convertColumnNameToDatabaseTableName(String columnName) {
        return columnName.toUpperCase();
    }
}