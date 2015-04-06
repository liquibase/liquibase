package liquibase.datatype.core;

import liquibase.database.Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.MySQLDatabase;
import liquibase.datatype.DataTypeInfo;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.LiquibaseDataType;
import liquibase.util.StringUtils;

@DataTypeInfo(name = "timestamp", aliases = {"java.sql.Types.TIMESTAMP", "java.sql.Timestamp", "timestamptz"}, minParameters = 0, maxParameters = 1, priority = LiquibaseDataType.PRIORITY_DEFAULT)
public class TimestampType extends DateTimeType {

    @Override
    public DatabaseDataType toDatabaseDataType(Database database) {
        String originalDefinition = StringUtils.trimToEmpty(getRawDefinition());
        if (database instanceof MySQLDatabase) {
            if (originalDefinition.contains(" ")) {
                return new DatabaseDataType(getRawDefinition());
            }
            return new DatabaseDataType("TIMESTAMP");
        }
        if (database instanceof MSSQLDatabase) {
            return new DatabaseDataType(database.escapeDataTypeName("datetime"));
        }
        return super.toDatabaseDataType(database);
    }
}
