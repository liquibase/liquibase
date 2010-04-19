package liquibase.database.structure.type;

import liquibase.database.Database;

public class ClobType extends DataType {

    public ClobType() {
        super("CLOB",0,0);
    }

    public ClobType(String dataTypeName) {
        super(dataTypeName,0,0);
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
    
}
