package liquibase.database.structure.type;

import liquibase.database.Database;

public class TextType extends DataType {
    public TextType(String dataTypeName, int minParameters, int maxParameters) {
        super(dataTypeName, minParameters, maxParameters);
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
