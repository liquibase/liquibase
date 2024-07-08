package liquibase.exception;

import liquibase.database.Database;

public class ChangeNotFoundException extends LiquibaseException {
    private static final long serialVersionUID = 3148141590098779029L;
    
    public ChangeNotFoundException(String name, Database database) {
        super("Change '"+name+"' not found or supported for "+database);
    }
}
