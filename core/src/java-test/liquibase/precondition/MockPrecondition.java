package liquibase.precondition;

import liquibase.database.Database;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.exception.PreconditionFailedException;
import liquibase.exception.PreconditionErrorException;

public class MockPrecondition implements Precondition {
    public String getName() {
        return "mock";
    }

    public void check(Database database, DatabaseChangeLog changeLog) throws PreconditionFailedException, PreconditionErrorException {
        
    }
}
