package liquibase.database.structure.type;

import liquibase.database.Database;

public class SmallIntType extends DataType {

    public SmallIntType() {
        super("SMALLINT", 0, 1);
    }

    public SmallIntType(String dataTypeName) {
        super(dataTypeName,0,1);
    }

    @Override
    public String convertObjectToString(Object value, Database database) {
        if (value == null) {
            return null;
        } else if (value.toString().equalsIgnoreCase("null")) {
            return "null";
        }
        if (value instanceof Boolean)
            return Boolean.TRUE.equals(value) ? "1" : "0";
        else
            return value.toString();
    }

}
