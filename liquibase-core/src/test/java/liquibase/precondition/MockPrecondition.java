package liquibase.precondition;

import liquibase.changelog.DatabaseChangeLog;
import liquibase.database.Database;
import liquibase.exception.PreconditionErrorException;
import liquibase.exception.PreconditionFailedException;

public class MockPrecondition implements Precondition {
    public String getName() {
        return "mock";
    }

    public void check(Database database, DatabaseChangeLog changeLog) throws PreconditionFailedException, PreconditionErrorException {
        
    }
}
