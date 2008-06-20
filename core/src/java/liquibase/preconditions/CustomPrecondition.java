package liquibase.preconditions;

import liquibase.database.Database;
import liquibase.DatabaseChangeLog;
import liquibase.exception.PreconditionFailedException;
import liquibase.exception.CustomPreconditionFailedException;

public interface CustomPrecondition {
    void check(Database database) throws CustomPreconditionFailedException;
}
