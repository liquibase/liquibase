package liquibase.database.typeconversion.core;

import liquibase.database.typeconversion.DataType;

public class HsqlTypeConverter extends DefaultTypeConverter {
    private static final DataType BOOLEAN_TYPE = new DataType("BOOLEAN", false);
    private static final DataType CURRENCY_TYPE = new DataType("DECIMAL", true);
    private static final DataType UUID_TYPE = new DataType("VARCHAR(36)", false);
    private static final DataType CLOB_TYPE = new DataType("LONGVARCHAR", true);
    private static final DataType BLOB_TYPE = new DataType("LONGVARBINARY", true);
    private static final DataType DATETIME_TYPE = new DataType("DATETIME", false);

     public DataType getBooleanType() {
        return BOOLEAN_TYPE;
    }

    public DataType getCurrencyType() {
        return CURRENCY_TYPE;
    }

    public DataType getUUIDType() {
        return UUID_TYPE;
    }

    public DataType getClobType() {
        return CLOB_TYPE;
    }

    public DataType getBlobType() {
        return BLOB_TYPE;
    }

    public DataType getDateTimeType() {
        return DATETIME_TYPE;
    }
}
