package liquibase.database.typeconversion.core;

import liquibase.database.structure.type.DateTimeType;
import liquibase.database.structure.type.UUIDType;
import liquibase.database.Database;
import liquibase.util.StringUtils;

import java.text.ParseException;

public class H2TypeConverter extends HsqlTypeConverter {

    @Override
    public Object convertDatabaseValueToJavaObject(Object defaultValue, int dataType, int columnSize, int decimalDigits, Database database) throws ParseException {
        if (defaultValue != null && defaultValue instanceof String) {
            if (StringUtils.trimToEmpty(((String) defaultValue)).startsWith("(NEXT VALUE FOR PUBLIC.SYSTEM_SEQUENCE_")) {
                return null;
            }
            if (StringUtils.trimToNull(((String) defaultValue)) == null) {
                return null;
            }
        }
        return super.convertDatabaseValueToJavaObject(defaultValue, dataType, columnSize, decimalDigits, database);
    }

    @Override
    public DateTimeType getDateTimeType() {
        return new DateTimeType() {
            @Override
            public String getDataTypeName() {
                return "TIMESTAMP";
            }
        };
    }

    @Override
    public UUIDType getUUIDType() {
        return new UUIDType() {
            @Override
            public String getDataTypeName() {
                return "VARCHAR(36)";
            }
        };
    }
}
