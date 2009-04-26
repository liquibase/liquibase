package liquibase.exception;

import liquibase.change.Change;

public class InvalidChangeDefinitionException extends LiquibaseException {

    public InvalidChangeDefinitionException(String message, Change change) {
        super(change.getName()+" in '"+change.getChangeSet().toString(false)+"' is invalid: "+message);
    }
}
