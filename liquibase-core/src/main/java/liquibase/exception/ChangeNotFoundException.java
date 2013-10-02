package liquibase.exception;

import liquibase.database.Database;

public class ChangeNotFoundException extends LiquibaseException {
    public ChangeNotFoundException(String name, Database database) {
        super("Change '"+name+"' not found or supported for "+database);
    }
}
