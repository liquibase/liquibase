package liquibase.change;

import liquibase.database.Database;
import liquibase.database.statement.SqlStatement;
import liquibase.database.structure.DatabaseObject;
import liquibase.exception.InvalidChangeDefinitionException;
import liquibase.exception.UnsupportedChangeException;
import liquibase.util.StringUtils;

import java.util.Set;

public class StopChange extends AbstractChange {

    private String message;

    public StopChange() {
        super("stop", "Stop Execution");
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = StringUtils.trimToNull(message);
    }

    public void validate(Database database) throws InvalidChangeDefinitionException {
        ;
    }

    public SqlStatement[] generateStatements(Database database) throws UnsupportedChangeException {
        throw new StopChangeException(getMessage());
    }

    public String getConfirmationMessage() {
        return "Changelog Execution Stopped";
    }

    public static class StopChangeException extends RuntimeException {
        public StopChangeException(String message) {
            super(message);
        }
    }
}
