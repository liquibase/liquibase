package liquibase.snapshot.jvm;

import liquibase.database.Database;
import liquibase.structure.core.Column;
import liquibase.database.core.H2Database;
import liquibase.exception.DatabaseException;
import liquibase.statement.DatabaseFunction;

import java.sql.SQLException;
import java.util.Map;

public class H2DatabaseSnapshotGenerator extends JdbcDatabaseSnapshotGenerator {
    public boolean supports(Database database) {
        return database instanceof H2Database;
    }

    public int getPriority(Database database) {
        return PRIORITY_DATABASE;
    }

    @Override
    protected Object readDefaultValue(Map<String, Object> columnMetadataResultSet, Column columnInfo, Database database) throws SQLException, DatabaseException {
        Object defaultValue = super.readDefaultValue(columnMetadataResultSet, columnInfo, database);
        if (defaultValue != null && defaultValue instanceof DatabaseFunction && ((DatabaseFunction) defaultValue).getValue().startsWith("NEXT VALUE FOR ")) {
            columnInfo.setAutoIncrement(true);
            return null;
        }
        return defaultValue;
    }
}