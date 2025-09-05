package liquibase.datatype.core;

import liquibase.change.core.LoadDataChange;
import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.datatype.DataTypeInfo;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.LiquibaseDataType;
import liquibase.servicelocator.PrioritizedService;
import liquibase.statement.DatabaseFunction;
import org.apache.commons.lang3.StringUtils;

@DataTypeInfo(name = "array", minParameters = 0, maxParameters = 1, priority = PrioritizedService.PRIORITY_DATABASE)
public class ArrayTypeSnowflake extends LiquibaseDataType {

    @Override
    public boolean supports(Database database) {
        return database instanceof SnowflakeDatabase;
    }

    @Override
    public DatabaseDataType toDatabaseDataType(Database database) {
        if (database instanceof SnowflakeDatabase) {
            // Support both ARRAY and ARRAY(elementType)
            if (getParameters() != null && getParameters().length > 0) {
                return new DatabaseDataType("ARRAY(" + getParameters()[0] + ")");
            }
            return new DatabaseDataType("ARRAY", getParameters());
        }
        return super.toDatabaseDataType(database);
    }

    @Override
    public LoadDataChange.LOAD_DATA_TYPE getLoadTypeName() {
        return LoadDataChange.LOAD_DATA_TYPE.STRING;
    }

    @Override
    public String objectToSql(Object value, Database database) {
        if (value instanceof String && database instanceof SnowflakeDatabase) {
            String stringValue = (String) value;
            // Handle array literals
            if (stringValue.startsWith("[") && stringValue.endsWith("]")) {
                return "PARSE_JSON('" + stringValue.replace("'", "''") + "')";
            }
        }
        return super.objectToSql(value, database);
    }

    @Override
    public Object sqlToObject(String value, Database database) {
        if (StringUtils.containsIgnoreCase(value, "array") || 
            StringUtils.containsIgnoreCase(value, "parse_json")) {
            return new DatabaseFunction(value);
        }
        return super.sqlToObject(value, database);
    }
}