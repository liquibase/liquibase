package liquibase.preconditions;

import liquibase.DatabaseChangeLog;
import liquibase.database.Database;
import liquibase.exception.PreconditionFailedException;

import java.util.ArrayList;
import java.util.List;

/**
 * Container class for all preconditions on a change log.
 */
public class AndPrecondition extends PreconditionLogic {

    private String onFail;

    public void check(Database database, DatabaseChangeLog changeLog) throws PreconditionFailedException {
        boolean allPassed = true;
        List<FailedPrecondition> failures = new ArrayList<FailedPrecondition>();
        for (Precondition precondition : getNestedPreconditions()) {
            try {
                precondition.check(database, changeLog);
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

    public String getOnFail() {
        return onFail;
    }

    public void setOnFail(String onFail) {
        this.onFail = onFail;
    }
}
