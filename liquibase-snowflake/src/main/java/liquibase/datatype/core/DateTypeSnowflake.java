package liquibase.datatype.core;

import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;

public class DateTypeSnowflake extends DateType {

    @Override
    public String objectToSql(Object value, Database database) {
        return String.format("TO_DATE(%s)", super.objectToSql(value, database));
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
