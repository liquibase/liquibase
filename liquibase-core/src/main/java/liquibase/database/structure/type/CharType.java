package liquibase.database.structure.type;

import liquibase.database.Database;

public class CharType extends DataType {

    public CharType() {
        super("CHAR",0,1);
    }

    public CharType(String dataTypeName) {
        super(dataTypeName,0,1);
    }

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
    public boolean getSupportsPrecision() {
        return true;
    }
}
