package liquibase.change;

import liquibase.database.Database;
import liquibase.statement.SqlStatement;
import liquibase.util.StringUtils;

public class StopChange extends AbstractChange {

    private String message;

    public StopChange() {
        super("stop", "Stop Execution", ChangeMetaData.PRIORITY_DEFAULT);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = StringUtils.trimToNull(message);
    }

    public SqlStatement[] generateStatements(Database database) {
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
