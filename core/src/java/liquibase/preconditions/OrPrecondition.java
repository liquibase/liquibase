package liquibase.preconditions;

import liquibase.DatabaseChangeLog;
import liquibase.database.Database;
import liquibase.exception.PreconditionErrorException;
import liquibase.exception.PreconditionFailedException;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for controling "or" logic in preconditions.
 */
public class OrPrecondition extends PreconditionLogic {


    public void check(Database database, DatabaseChangeLog changeLog) throws PreconditionFailedException, PreconditionErrorException {
        boolean onePassed = false;
        List<FailedPrecondition> failures = new ArrayList<FailedPrecondition>();
        for (Precondition precondition : getNestedPreconditions()) {
            try {
                precondition.check(database, changeLog);
                onePassed = true;
            } catch (PreconditionFailedException e) {
                failures.addAll(e.getFailedPreconditions());
            }
        }
        if (!onePassed) {
            throw new PreconditionFailedException(failures);
        }
    }

    public String getTagName() {
        return "or";
    }
}
