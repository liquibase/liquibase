package liquibase.preconditions;

import liquibase.DatabaseChangeLog;
import liquibase.exception.PreconditionFailedException;
import liquibase.migrator.Migrator;

/**
 * Class for controling "not" logic in preconditions.
 */
public class NotPrecondition extends PreconditionLogic {


    public void check(Migrator migrator, DatabaseChangeLog changeLog) throws PreconditionFailedException {
        for (Precondition precondition : getNestedPreconditions()) {
            boolean threwException = false;
            try {
                precondition.check(migrator, changeLog);
            } catch (PreconditionFailedException e) {
                ; //that's what we want with a Not precondition
                threwException = true;
            }

            if (!threwException) {
                throw new PreconditionFailedException("Not precondition failed", changeLog, this);
            }
        }
    }

    public String getTagName() {
        return "not";
    }
}
