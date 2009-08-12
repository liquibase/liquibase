package liquibase.database.typeconversion.core;

import liquibase.database.typeconversion.DataType;
import liquibase.database.Database;

public class MaxDBTypeConverter extends DefaultTypeConverter {

    private static final DataType BOOLEAN_TYPE = new DataType("BOOLEAN", false);
    private static final DataType CURRENCY_TYPE = new DataType("NUMERIC(15, 2)", false);
    private static final DataType UUID_TYPE = new DataType("CHAR(36)", false);
    private static final DataType CLOB_TYPE = new DataType("LONG VARCHAR", false);
    private static final DataType BLOB_TYPE = new DataType("LONG BYTE", false);
    private static final DataType DATETIME_TYPE = new DataType("TIMESTAMP", false);
    private static final DataType DATE_TYPE = new DataType("DATE", false);
    private static final DataType TIME_TYPE = new DataType("TIME", false);

    @Override
    public String convertJavaObjectToString(Object value, Database database) {
        if (value instanceof Boolean) {
            if (((Boolean) value)) {
                return this.getTrueBooleanValue();
            } else {
                return this.getFalseBooleanValue();
            }
        } else {
            return super.convertJavaObjectToString(value, database);
        }
    }
    
    @Override
    public String getTrueBooleanValue() {
        return "TRUE";
    }

    @Override
    public String getFalseBooleanValue() {
        return "FALSE";
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

    @Override
    public DataType getDateType() {
        return DATE_TYPE;
    }

    @Override
    public DataType getTimeType() {
        return TIME_TYPE;
    }
    

}
