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

@DataTypeInfo(name = "geography", aliases = {"geospatial", "gis"}, minParameters = 0, maxParameters = 1, priority = PrioritizedService.PRIORITY_DATABASE)
public class GeographyTypeSnowflake extends LiquibaseDataType {

    @Override
    public boolean supports(Database database) {
        return database instanceof SnowflakeDatabase;
    }

    @Override
    public DatabaseDataType toDatabaseDataType(Database database) {
        if (database instanceof SnowflakeDatabase) {
            return new DatabaseDataType("GEOGRAPHY", getParameters());
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
            // Handle WKT (Well-Known Text) and GeoJSON formats
            if (stringValue.contains("POINT") || stringValue.contains("POLYGON") || 
                stringValue.contains("LINESTRING") || stringValue.contains("coordinates")) {
                return "ST_GEOGFROMTEXT('" + stringValue.replace("'", "''") + "')";
            }
        }
        return super.objectToSql(value, database);
    }

    @Override
    public Object sqlToObject(String value, Database database) {
        if (StringUtils.containsIgnoreCase(value, "st_geog") || 
            StringUtils.containsIgnoreCase(value, "geography")) {
            return new DatabaseFunction(value);
        }
        return super.sqlToObject(value, database);
    }
}