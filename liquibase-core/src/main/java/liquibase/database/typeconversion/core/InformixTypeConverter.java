package liquibase.database.typeconversion.core;

import liquibase.database.Database;
import liquibase.database.structure.type.BigIntType;
import liquibase.database.structure.type.CurrencyType;
import liquibase.database.structure.type.DateTimeType;
import liquibase.database.structure.type.TimeType;

import java.util.regex.Pattern;

public class InformixTypeConverter extends DefaultTypeConverter {

    private static final Pattern INTEGER_PATTERN = Pattern.compile("^(int(eger)?)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern INTEGER8_PATTERN = Pattern.compile("^(int(eger)?8)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern SERIAL_PATTERN = Pattern.compile("^(serial)(\\s*\\(\\d+\\)|)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern SERIAL8_PATTERN = Pattern.compile("^(serial8)(\\s*\\(\\d+\\)|)$", Pattern.CASE_INSENSITIVE);

    private static final String INTERVAL_FIELD_QUALIFIER = "HOUR TO FRACTION(5)";
    private static final String DATETIME_FIELD_QUALIFIER = "YEAR TO FRACTION(5)";

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
    public BigIntType getBigIntType() {
        return new BigIntType() {
            @Override
            public String getDataTypeName() {
                return "INT8";
            }
        };
    }

    @Override
    public CurrencyType getCurrencyType() {
        return new CurrencyType() {
            @Override
            public String getDataTypeName() {
                return "MONEY";
            }
        };
    }

    @Override
    public DateTimeType getDateTimeType() {
        return new DateTimeType() {
            @Override
            public String getDataTypeName() {
                return "DATETIME " + DATETIME_FIELD_QUALIFIER;
            }
        };
    }

    @Override
    public TimeType getTimeType() {
        return new TimeType() {
            @Override
            public String getDataTypeName() {
                return "INTERVAL " + INTERVAL_FIELD_QUALIFIER;
            }
        };
    }
}
