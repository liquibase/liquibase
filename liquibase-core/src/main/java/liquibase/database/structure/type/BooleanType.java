package liquibase.database.structure.type;

import liquibase.database.Database;
import liquibase.database.typeconversion.TypeConverterFactory;
import liquibase.database.typeconversion.TypeConverter;

public class BooleanType extends DataType {

    @Override
    public String getDataTypeName() {
        return "BOOLEAN";
    }

    @Override
    public String convertObjectToString(Object value, Database database) {
        if (value == null) {
            return null;
        }
        String returnValue;
        TypeConverter converter = TypeConverterFactory.getInstance().findTypeConverter(database);
        if (((Boolean) value)) {
            returnValue = converter.getTrueBooleanValue();
        } else {
            returnValue = converter.getFalseBooleanValue();
        }
        if (returnValue.matches("\\d+")) {
            return returnValue;
        } else {
            return "'" + returnValue + "'";
        }

    }
}
