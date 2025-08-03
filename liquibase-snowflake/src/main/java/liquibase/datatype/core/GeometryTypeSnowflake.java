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

@DataTypeInfo(name = "geometry", aliases = {"planar-geometry"}, minParameters = 0, maxParameters = 0, priority = PrioritizedService.PRIORITY_DATABASE)
public class GeometryTypeSnowflake extends LiquibaseDataType {

    @Override
    public boolean supports(Database database) {
        return database instanceof SnowflakeDatabase;
    }

    @Override
    public DatabaseDataType toDatabaseDataType(Database database) {
        if (database instanceof SnowflakeDatabase) {
            return new DatabaseDataType("GEOMETRY", getParameters());
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
            // Handle WKT (Well-Known Text) formats for planar geometry
            if (stringValue.contains("POINT") || stringValue.contains("POLYGON") || 
                stringValue.contains("LINESTRING") || stringValue.contains("MULTIPOINT")) {
                return "ST_GEOMFROMTEXT('" + stringValue.replace("'", "''") + "')";
            }
        }
        return super.objectToSql(value, database);
    }

    @Override
    public Object sqlToObject(String value, Database database) {
        if (StringUtils.containsIgnoreCase(value, "st_geom") || 
            StringUtils.containsIgnoreCase(value, "geometry")) {
            return new DatabaseFunction(value);
        }
        return super.sqlToObject(value, database);
    }
}