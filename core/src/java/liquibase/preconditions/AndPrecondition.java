package liquibase.preconditions;

import liquibase.DatabaseChangeLog;
import liquibase.exception.PreconditionFailedException;
import liquibase.migrator.Migrator;

import java.util.ArrayList;
import java.util.List;

/**
 * Container class for all preconditions on a change log.
 */
public class AndPrecondition extends PreconditionLogic {

    public void check(Migrator migrator, DatabaseChangeLog changeLog) throws PreconditionFailedException {
        boolean allPassed = true;
        List<FailedPrecondition> failures = new ArrayList<FailedPrecondition>();
        for (Precondition precondition : getNestedPreconditions()) {
            try {
                precondition.check(migrator, changeLog);
            } catch (PreconditionFailedException e) {
                failures.addAll(e.getFailedPreconditions());
                allPassed = false;
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
