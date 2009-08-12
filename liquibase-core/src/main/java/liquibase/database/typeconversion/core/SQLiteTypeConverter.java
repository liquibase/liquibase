package liquibase.database.typeconversion.core;

import liquibase.database.typeconversion.DataType;

import java.util.Locale;

public class SQLiteTypeConverter extends DefaultTypeConverter {

    private static final DataType BLOB_TYPE = new DataType("BLOB", false);
    private static final DataType BOOLEAN_TYPE = new DataType("BOOLEAN", false);
    private static final DataType CLOB_TYPE = new DataType("TEXT", true);
    private static final DataType CURRENCY_TYPE = new DataType("REAL", false);
    private static final DataType DATETIME_TYPE = new DataType("TEXT", false);

    @Override
    public String getColumnType(String columnType, Boolean autoIncrement) {
        String type;
        if (columnType.equals("INTEGER") ||
                columnType.toLowerCase(Locale.ENGLISH).contains("int") ||
                columnType.toLowerCase(Locale.ENGLISH).contains("bit")) {
            type = "INTEGER";
        } else if (columnType.equals("TEXT") ||
                columnType.toLowerCase(Locale.ENGLISH).contains("uuid") ||
                columnType.toLowerCase(Locale.ENGLISH).contains("uniqueidentifier") ||

                columnType.toLowerCase(Locale.ENGLISH).equals("uniqueidentifier") ||
                columnType.toLowerCase(Locale.ENGLISH).equals("datetime") ||
                columnType.toLowerCase(Locale.ENGLISH).contains("timestamp") ||
                columnType.toLowerCase(Locale.ENGLISH).contains("char") ||
                columnType.toLowerCase(Locale.ENGLISH).contains("clob") ||
                columnType.toLowerCase(Locale.ENGLISH).contains("text")) {
            type = "TEXT";
        } else if (columnType.equals("REAL") ||
                columnType.toLowerCase(Locale.ENGLISH).contains("float")) {
            type = "REAL";
        } else if (columnType.toLowerCase(Locale.ENGLISH).contains("blob") ||
                columnType.toLowerCase(Locale.ENGLISH).contains("binary")) {
            type = "BLOB";
        } else if (columnType.toLowerCase(Locale.ENGLISH).contains("boolean") ||
                columnType.toLowerCase(Locale.ENGLISH).contains("binary")) {
            type = "BOOLEAN";
        } else {
            type = super.getColumnType(columnType, autoIncrement);
        }
        return type;
    }

    @Override
    public String getFalseBooleanValue() {
        return "0";
    }

    @Override
    public String getTrueBooleanValue() {
        return "1";
    }

    @Override
    public DataType getBlobType() {
        return BLOB_TYPE;
    }

    @Override
    public DataType getBooleanType() {
        return BOOLEAN_TYPE;
    }

    @Override
    public DataType getClobType() {
        return CLOB_TYPE;
    }

    @Override
    public DataType getCurrencyType() {
        return CURRENCY_TYPE;
    }

    @Override
    public DataType getDateTimeType() {
        return DATETIME_TYPE;
    }

    @Override
    public DataType getUUIDType() {
        return DATETIME_TYPE;
    }

}
