package liquibase.datatype.core;

import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;

public class TimestampTypeSnowflake extends TimestampType {

    @Override
    public String objectToSql(Object value, Database database) {
        return String.format("TO_TIMESTAMP(%s)", super.objectToSql(value, database));
    }

    @Override
    public boolean supports(Database database) {
        return database instanceof SnowflakeDatabase;
    }

    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }
}
