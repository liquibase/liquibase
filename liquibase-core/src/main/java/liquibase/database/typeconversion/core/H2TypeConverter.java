package liquibase.database.typeconversion.core;

import liquibase.database.structure.type.DateTimeType;
import liquibase.database.structure.type.UUIDType;
import liquibase.database.structure.type.ClobType;
import liquibase.database.structure.type.BlobType;
import liquibase.database.Database;
import liquibase.database.core.H2Database;
import liquibase.util.StringUtils;

import java.text.ParseException;

public class H2TypeConverter extends AbstractTypeConverter {

    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    public boolean supports(Database database) {
        return database instanceof H2Database;
    }

    @Override
    public Object convertDatabaseValueToObject(Object defaultValue, int dataType, int columnSize, int decimalDigits, Database database) throws ParseException {
        if (defaultValue != null && defaultValue instanceof String) {
            if (StringUtils.trimToEmpty(((String) defaultValue)).startsWith("(NEXT VALUE FOR PUBLIC.SYSTEM_SEQUENCE_")) {
                return null;
            }
            if (StringUtils.trimToNull(((String) defaultValue)) == null) {
                return null;
            }
        }
        return super.convertDatabaseValueToObject(defaultValue, dataType, columnSize, decimalDigits, database);
    }

    @Override
    public DateTimeType getDateTimeType() {
        return new DateTimeType("TIMESTAMP");
    }

    @Override
    public UUIDType getUUIDType() {
        return new UUIDType("UUID");
    }

    @Override
    public ClobType getClobType() {
        return new ClobType("LONGVARCHAR");
    }

    @Override
    public BlobType getBlobType() {
        return new BlobType("LONGVARBINARY");
    }
}
