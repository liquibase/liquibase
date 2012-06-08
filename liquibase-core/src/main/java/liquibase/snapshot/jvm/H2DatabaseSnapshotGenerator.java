package liquibase.snapshot.jvm;

import java.sql.ResultSet;
import java.sql.SQLException;

import liquibase.database.Database;
import liquibase.database.core.H2Database;
import liquibase.database.structure.Column;
import liquibase.exception.DatabaseException;
import liquibase.statement.DatabaseFunction;

public class H2DatabaseSnapshotGenerator extends JdbcDatabaseSnapshotGenerator {
    public boolean supports(Database database) {
        return database instanceof H2Database;
    }

    public int getPriority(Database database) {
        return PRIORITY_DATABASE;
    }

    @Override
    protected Object readDefaultValue(ResultSet columnMetadataResultSet, Column columnInfo, Database database) throws SQLException, DatabaseException {
        Object defaultValue = super.readDefaultValue(columnMetadataResultSet, columnInfo, database);
        if (defaultValue != null && defaultValue instanceof DatabaseFunction && ((DatabaseFunction) defaultValue).getValue().startsWith("NEXT VALUE FOR ")) {
            columnInfo.setAutoIncrement(true);
            return null;
        }
        return defaultValue;
    }
}