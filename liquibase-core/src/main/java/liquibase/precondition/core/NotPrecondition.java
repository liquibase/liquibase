package liquibase.precondition.core;

import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.exception.PreconditionErrorException;
import liquibase.exception.PreconditionFailedException;
import liquibase.exception.ValidationErrors;
import liquibase.exception.Warnings;
import liquibase.precondition.Precondition;
import liquibase.precondition.PreconditionLogic;

/**
 * Class for controling "not" logic in preconditions.
 */
public class NotPrecondition extends PreconditionLogic {

    public Warnings warn(Database database) {
        return new Warnings();
    }

    public ValidationErrors validate(Database database) {
        return new ValidationErrors();
    }
    
    public void check(Database database, DatabaseChangeLog changeLog, ChangeSet changeSet) throws PreconditionFailedException, PreconditionErrorException {
        for (Precondition precondition : getNestedPreconditions()) {
            boolean threwException = false;
            try {
                precondition.check(database, changeLog, changeSet);
            } catch (PreconditionFailedException e) {
                ; //that's what we want with a Not precondition
                threwException = true;
            }

            if (!threwException) {
                throw new PreconditionFailedException("Not precondition failed", changeLog, this);
            }
        }
    }

    public String getName() {
        return "not";
    }
}
