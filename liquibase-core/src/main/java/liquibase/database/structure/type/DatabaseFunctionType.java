package liquibase.database.structure.type;

import liquibase.database.Database;

public class DatabaseFunctionType extends DataType {
    public DatabaseFunctionType() {
        super("FUNCTION", 0, 0);
    }

    @Override
    public String convertObjectToString(Object value, Database database) {
        if (value.toString().equals("CURRENT_TIMESTAMP()")) {
            return database.getCurrentDateTimeFunction();
        }

        return value.toString();
    }
}
