package liquibase.util.ui;

import liquibase.database.Database;
import liquibase.exception.DatabaseException;

public interface UIFacade {

    boolean promptForNonLocalDatabase(Database database) throws DatabaseException;
}
