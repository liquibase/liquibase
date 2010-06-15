package liquibase.database.structure.type;

import liquibase.database.Database;
import liquibase.database.typeconversion.TypeConverterFactory;
import liquibase.database.typeconversion.TypeConverter;

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
        if (((Boolean) value)) {
            returnValue = converter.getBooleanType().getTrueBooleanValue();
        } else {
            returnValue = converter.getBooleanType().getFalseBooleanValue();
        }
        if (returnValue.matches("\\d+")) {
            return returnValue;
        } else {
            return "'" + returnValue + "'";
        }

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
