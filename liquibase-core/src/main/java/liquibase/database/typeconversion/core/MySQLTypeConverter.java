package liquibase.database.typeconversion.core;

import liquibase.database.typeconversion.DataType;

public class MySQLTypeConverter extends DefaultTypeConverter {

    private static final DataType BOOLEAN_TYPE = new DataType("TINYINT(1)", false);
    private static final DataType CURRENCY_TYPE = new DataType("DECIMAL", true);
    private static final DataType UUID_TYPE = new DataType("CHAR(36)", false);
    private static final DataType CLOB_TYPE = new DataType("TEXT", true);
    private static final DataType BLOB_TYPE = new DataType("BLOB", true);
    private static final DataType DATETIME_TYPE = new DataType("DATETIME", false);

    @Override
    public String getFalseBooleanValue() {
        return "0";
    }

    @Override
    public String getTrueBooleanValue() {
        return "1";
    }

    @Override
    public DataType getBooleanType() {
        return BOOLEAN_TYPE;
    }

    @Override
    public DataType getCurrencyType() {
        return CURRENCY_TYPE;
    }

    @Override
    public DataType getUUIDType() {
        return UUID_TYPE;
    }

    @Override
    public DataType getClobType() {
        return CLOB_TYPE;
    }

    @Override
    public DataType getBlobType() {
        return BLOB_TYPE;
    }

    @Override
    public DataType getDateTimeType() {
        return DATETIME_TYPE;
    }

}
