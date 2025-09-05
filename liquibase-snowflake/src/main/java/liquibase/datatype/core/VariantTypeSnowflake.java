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

@DataTypeInfo(name = "variant", aliases = {"json", "semi-structured"}, minParameters = 0, maxParameters = 0, priority = PrioritizedService.PRIORITY_DATABASE)
public class VariantTypeSnowflake extends LiquibaseDataType {

    @Override
    public boolean supports(Database database) {
        return database instanceof SnowflakeDatabase;
    }

    @Override
    public DatabaseDataType toDatabaseDataType(Database database) {
        if (database instanceof SnowflakeDatabase) {
            return new DatabaseDataType("VARIANT", getParameters());
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
            // Handle JSON string literals
            if (stringValue.startsWith("{") || stringValue.startsWith("[")) {
                return "PARSE_JSON('" + stringValue.replace("'", "''") + "')";
            }
        }
        return super.objectToSql(value, database);
    }

    @Override
    public Object sqlToObject(String value, Database database) {
        if (StringUtils.containsIgnoreCase(value, "parse_json")) {
            return new DatabaseFunction(value);
        }
        return super.sqlToObject(value, database);
    }
}