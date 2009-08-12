package liquibase.database.typeconversion.core;

import liquibase.database.typeconversion.DataType;

public class FirebirdTypeConverter extends DefaultTypeConverter {

    private static final DataType BOOLEAN_TYPE = new DataType("SMALLINT", false);
    private static final DataType CURRENCY_TYPE = new DataType("DECIMAL(18, 4)", false);
    private static final DataType UUID_TYPE = new DataType("CHAR(36)", false);
    private static final DataType CLOB_TYPE = new DataType("BLOB SUB_TYPE TEXT", false);
    private static final DataType BLOB_TYPE = new DataType("BLOB", false);
    private static final DataType DATETIME_TYPE = new DataType("TIMESTAMP", false);

    @Override
    public String getColumnType(String columnType, Boolean autoIncrement) {
        String type = super.getColumnType(columnType, autoIncrement);
        if (type.startsWith("BLOB SUB_TYPE <0")) {
            return getBlobType().getDataTypeName();
        } else {
            return type;
        }
    }

    @Override
    public String getTrueBooleanValue() {
        return "1";
    }

    @Override
    public String getFalseBooleanValue() {
        return "0";
    }

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
