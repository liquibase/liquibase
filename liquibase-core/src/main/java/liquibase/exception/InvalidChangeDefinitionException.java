package liquibase.exception;

import liquibase.change.Change;
import liquibase.change.ChangeFactory;

public class InvalidChangeDefinitionException extends LiquibaseException {

    public InvalidChangeDefinitionException(String message, Change change) {
        super(ChangeFactory.getInstance().getChangeMetaData(change).getName()+" in '"+change.getChangeSet().toString(false)+"' is invalid: "+message);
    }
}
