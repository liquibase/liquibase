package liquibase.preconditions;

import liquibase.DatabaseChangeLog;
import liquibase.database.Database;
import liquibase.exception.PreconditionFailedException;
import liquibase.exception.PreconditionErrorException;

/**
 * Class for controling "not" logic in preconditions.
 */
public class NotPrecondition extends PreconditionLogic {


    public void check(Database database, DatabaseChangeLog changeLog) throws PreconditionFailedException, PreconditionErrorException {
        for (Precondition precondition : getNestedPreconditions()) {
            boolean threwException = false;
            try {
                precondition.check(database, changeLog);
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
