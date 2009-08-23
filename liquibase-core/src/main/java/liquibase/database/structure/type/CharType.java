package liquibase.database.structure.type;

import liquibase.database.Database;

public class CharType extends DataType {

    @Override
    public String convertObjectToString(Object value, Database database) {
        if (value == null) {
            return null;
        }
        if ("null".equalsIgnoreCase(((String) value))) {
            return null;
        }
        return "'" + ((String) value).replaceAll("'", "''") + "'";
    }

    @Override
    public String getDataTypeName() {
        return "CHAR";
    }

    @Override
    public boolean getSupportsPrecision() {
        return true;
    }
}
