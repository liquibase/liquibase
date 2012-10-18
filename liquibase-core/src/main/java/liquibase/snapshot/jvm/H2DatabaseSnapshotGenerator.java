package liquibase.snapshot.jvm;

import liquibase.database.Database;
import liquibase.database.structure.Column;
import liquibase.database.structure.Schema;
import liquibase.database.structure.Table;
import liquibase.database.core.H2Database;
import liquibase.exception.DatabaseException;
import liquibase.statement.DatabaseFunction;

import java.sql.ResultSet;
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
    protected Object readDefaultValue(Map<String, String> columnMetadataResultSet, Column columnInfo, Database database) throws SQLException, DatabaseException {
        Object defaultValue = super.readDefaultValue(columnMetadataResultSet, columnInfo, database);
        if (defaultValue != null && defaultValue instanceof DatabaseFunction && ((DatabaseFunction) defaultValue).getValue().startsWith("NEXT VALUE FOR ")) {
            columnInfo.setAutoIncrement(true);
            return null;
        }
        return defaultValue;
    }
}