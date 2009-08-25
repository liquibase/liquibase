package liquibase.database.typeconversion.core;

import liquibase.database.Database;
import liquibase.database.core.InformixDatabase;
import liquibase.database.structure.type.*;

import java.util.regex.Pattern;

public class InformixTypeConverter extends AbstractTypeConverter {

    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    public boolean supports(Database database) {
        return database instanceof InformixDatabase;
    }


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
    public BooleanType getBooleanType() {
        return new BooleanType() {
            @Override
            public String getTrueBooleanValue() {
                return "'t'";
            }

            @Override
            public String getFalseBooleanValue() {
                return "'f'";
            }
        };
    }

    @Override
    public BigIntType getBigIntType() {
        return new BigIntType("INT8");
    }

    @Override
    public CurrencyType getCurrencyType() {
        return new CurrencyType("MONEY");
    }

    @Override
    public DateTimeType getDateTimeType() {
        return new DateTimeType("DATETIME " + DATETIME_FIELD_QUALIFIER);
    }

    @Override
    public TimeType getTimeType() {
        return new TimeType("INTERVAL " + INTERVAL_FIELD_QUALIFIER);
    }
}
