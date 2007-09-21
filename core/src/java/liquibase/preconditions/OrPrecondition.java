package liquibase.preconditions;

import liquibase.DatabaseChangeLog;
import liquibase.exception.PreconditionFailedException;
import liquibase.migrator.Migrator;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for controling "or" logic in preconditions.
 */
public class OrPrecondition extends PreconditionLogic {


    public void check(Migrator migrator, DatabaseChangeLog changeLog) throws PreconditionFailedException {
        boolean onePassed = false;
        List<FailedPrecondition> failures = new ArrayList<FailedPrecondition>();
        for (Precondition precondition : getNestedPreconditions()) {
            try {
                precondition.check(migrator, changeLog);
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
