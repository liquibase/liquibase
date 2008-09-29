package liquibase.preconditions;

import liquibase.DatabaseChangeLog;
import liquibase.database.Database;
import liquibase.exception.PreconditionFailedException;
import liquibase.exception.PreconditionErrorException;

import java.util.ArrayList;
import java.util.List;

/**
 * Container class for all preconditions on a change log.
 */
public class AndPrecondition extends PreconditionLogic {

    public void check(Database database, DatabaseChangeLog changeLog) throws PreconditionFailedException, PreconditionErrorException {
        boolean allPassed = true;
        List<FailedPrecondition> failures = new ArrayList<FailedPrecondition>();
        for (Precondition precondition : getNestedPreconditions()) {
            try {
                precondition.check(database, changeLog);
            } catch (PreconditionFailedException e) {
                failures.addAll(e.getFailedPreconditions());
                allPassed = false;
                break;
            }
        }
        if (!allPassed) {
            throw new PreconditionFailedException(failures);
        }
    }


    public String getTagName() {
        return "and";
    }
}
