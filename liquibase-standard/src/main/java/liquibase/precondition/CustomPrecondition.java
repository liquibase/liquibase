package liquibase.precondition;

import liquibase.database.Database;
import liquibase.exception.CustomPreconditionErrorException;
import liquibase.exception.CustomPreconditionFailedException;

public interface CustomPrecondition {
    void check(Database database) throws CustomPreconditionFailedException, CustomPreconditionErrorException;
}
