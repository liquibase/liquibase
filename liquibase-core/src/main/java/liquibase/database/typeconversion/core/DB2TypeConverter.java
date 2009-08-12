package liquibase.database.typeconversion.core;

import liquibase.database.typeconversion.DataType;
import liquibase.database.Database;

import java.text.ParseException;
import java.sql.Types;

public class DB2TypeConverter extends DefaultTypeConverter {

    private static final DataType BOOLEAN_TYPE = new DataType("SMALLINT", true);
    private static final DataType CURRENCY_TYPE = new DataType("DECIMAL(19,4)", false);
    private static final DataType UUID_TYPE = new DataType("VARCHAR(36)", false);
    private static final DataType CLOB_TYPE = new DataType("CLOB", true);
    private static final DataType BLOB_TYPE = new DataType("BLOB", true);
    private static final DataType DATETIME_TYPE = new DataType("TIMESTAMP", false);

    @Override
    public Object convertDatabaseValueToJavaObject(Object defaultValue, int dataType, int columnSize, int decimalDigits, Database database) throws ParseException {
        if (defaultValue != null && defaultValue instanceof String) {
            if (dataType == Types.TIMESTAMP) {
                defaultValue = ((String) defaultValue).replaceFirst("^\"SYSIBM\".\"TIMESTAMP\"\\('", "").replaceFirst("'\\)", "");
            } else if (dataType == Types.TIME) {
                defaultValue = ((String) defaultValue).replaceFirst("^\"SYSIBM\".\"TIME\"\\('", "").replaceFirst("'\\)", "");
            } else if (dataType == Types.DATE) {
                defaultValue = ((String) defaultValue).replaceFirst("^\"SYSIBM\".\"DATE\"\\('", "").replaceFirst("'\\)", "");
            }
        }
        return super.convertDatabaseValueToJavaObject(defaultValue, dataType, columnSize, decimalDigits, database);
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
