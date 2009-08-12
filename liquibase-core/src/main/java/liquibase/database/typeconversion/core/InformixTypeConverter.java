package liquibase.database.typeconversion.core;

import liquibase.database.typeconversion.DataType;
import liquibase.database.Database;

import java.util.regex.Pattern;

public class InformixTypeConverter extends DefaultTypeConverter {

    private static final Pattern INTEGER_PATTERN = Pattern.compile("^(int(eger)?)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern INTEGER8_PATTERN =  Pattern.compile("^(int(eger)?8)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern SERIAL_PATTERN = Pattern.compile("^(serial)(\\s*\\(\\d+\\)|)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern SERIAL8_PATTERN = Pattern.compile("^(serial8)(\\s*\\(\\d+\\)|)$", Pattern.CASE_INSENSITIVE);

    private static final String INTERVAL_FIELD_QUALIFIER = "HOUR TO FRACTION(5)";
    private static final String DATETIME_FIELD_QUALIFIER = "YEAR TO FRACTION(5)";
    
    private static final DataType UUID_TYPE = new DataType("VARCHAR(36)", false);
    private static final DataType CURRENCY_TYPE = new DataType("MONEY", true);
    private static final DataType CLOB_TYPE = new DataType("CLOB", false);
    private static final DataType BOOLEAN_TYPE = new DataType("BOOLEAN", false);
    private static final DataType BLOB_TYPE = new DataType("BLOB", false);
    private static final DataType BIGINT_TYPE = new DataType("INT8", false);
    private static final DataType TIME_TYPE = new DataType("INTERVAL " + INTERVAL_FIELD_QUALIFIER, false);
    private static final DataType DATETIME_TYPE = new DataType("DATETIME " + DATETIME_FIELD_QUALIFIER, false);

    @Override
    public String getColumnType(String columnType, Boolean autoIncrement) {
        String type = super.getColumnType(columnType, autoIncrement);
        if (autoIncrement != null && autoIncrement) {
            if (isSerial(type)) {
                return "SERIAL";
            } else if (isSerial8(type)) {
                return "SERIAL8";
            } else {
                throw new IllegalArgumentException("Unknown autoincrement type: " + columnType);
            }
        }
        return type;
    }

    @Override
public String convertJavaObjectToString(Object value, Database database) {
    if (value != null && value instanceof Boolean) {
        if (((Boolean) value)) {
            return getTrueBooleanValue();
        } else {
            return getFalseBooleanValue();
        }
    }
    return super.convertJavaObjectToString(value, database);
}
    

    private boolean isSerial(String type) {
        return INTEGER_PATTERN.matcher(type).matches()
                || SERIAL_PATTERN.matcher(type).matches();
    }

    private boolean isSerial8(String type) {
        return INTEGER8_PATTERN.matcher(type).matches()
                || SERIAL8_PATTERN.matcher(type).matches()
                || "BIGINT".equals(type.toUpperCase());
    }

    @Override
    public String getTrueBooleanValue() {
        return "'t'";
    }

    @Override
    public String getFalseBooleanValue() {
        return "'f'";
    }

    @Override
    public DataType getBigIntType() {
        return BIGINT_TYPE;
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
    public DataType getTimeType() {
        return TIME_TYPE;
    }

    @Override
    public DataType getUUIDType() {
        return UUID_TYPE;
    }

    

}
