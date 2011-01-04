package liquibase.database.structure.type;

import liquibase.database.Database;
import liquibase.database.typeconversion.TypeConverterFactory;
import liquibase.database.typeconversion.TypeConverter;
import liquibase.exception.UnexpectedLiquibaseException;

public class BooleanType extends DataType {

    public BooleanType() {
        super("BOOLEAN",0,0);
    }

    public BooleanType(String dataTypeName) {
        super(dataTypeName,0,0);
    }

    @Override
    public String convertObjectToString(Object value, Database database) {
        if (value == null) {
            return null;
        } else if (value.toString().equalsIgnoreCase("null")) {
            return "null";
        }

        String returnValue;
        TypeConverter converter = TypeConverterFactory.getInstance().findTypeConverter(database);
        BooleanType booleanType = converter.getBooleanType();
        if (value instanceof String) {
            if (((String) value).equalsIgnoreCase("true") || value.equals("1") || ((String) value).equalsIgnoreCase(booleanType.getTrueBooleanValue())) {
                returnValue = booleanType.getTrueBooleanValue();
            } else if (((String) value).equalsIgnoreCase("false") || value.equals("0") || ((String) value).equalsIgnoreCase(booleanType.getFalseBooleanValue())) {
                returnValue = booleanType.getTrueBooleanValue();
            } else {
                throw new UnexpectedLiquibaseException("Unknown boolean value: "+value);
            }
        } else if (value instanceof Long) {
            if (Long.valueOf(1).equals(value)) {
                returnValue = booleanType.getTrueBooleanValue();
            } else {
                returnValue = booleanType.getFalseBooleanValue();
            }
        } else if (((Boolean) value)) {
            returnValue = booleanType.getTrueBooleanValue();
        } else {
            returnValue = booleanType.getFalseBooleanValue();
        }

        return returnValue;
    }

    /**
     * The database-specific value to use for "false" "boolean" columns.
     */
    public String getFalseBooleanValue() {
        return "FALSE";
    }

    /**
     * The database-specific value to use for "true" "boolean" columns.
     */
    public String getTrueBooleanValue() {
        return "TRUE";
    }

    public static class NumericBooleanType extends BooleanType {

        public NumericBooleanType() {
            super("INT");
        }

        public NumericBooleanType(String dataTypeName) {
            super(dataTypeName);
        }

        @Override
        public String getFalseBooleanValue() {
            return "0";
        }

        @Override
        public String getTrueBooleanValue() {
            return "1";
        }
    }
}
