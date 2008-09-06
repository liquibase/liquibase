package liquibase.preconditions;

import liquibase.database.Database;
import liquibase.DatabaseChangeLog;
import liquibase.exception.PreconditionFailedException;
import liquibase.exception.CustomPreconditionFailedException;
import liquibase.exception.CustomPreconditionErrorException;

public interface CustomPrecondition {
    void check(Database database) throws CustomPreconditionFailedException, CustomPreconditionErrorException;
}
