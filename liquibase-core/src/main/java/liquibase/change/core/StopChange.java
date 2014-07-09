package liquibase.change.core;

import liquibase.action.Action;
import liquibase.action.ExecuteAction;
import liquibase.change.AbstractChange;
import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;
import  liquibase.ExecutionEnvironment;
import liquibase.exception.DatabaseException;
import liquibase.executor.ExecuteResult;
import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;
import liquibase.resource.ResourceAccessor;
import liquibase.statement.Statement;
import liquibase.statement.core.RawActionStatement;
import liquibase.util.StringUtils;

@DatabaseChange(name="stop", description = "Stops Liquibase execution with a message. Mainly useful for debugging and stepping through a changelog", priority = ChangeMetaData.PRIORITY_DEFAULT, since = "1.9")
public class StopChange extends AbstractChange {

    private String message ="Stop command in changelog file";

    @Override
    public boolean generateStatementsVolatile(ExecutionEnvironment env) {
        return true;
    }

    @DatabaseChangeProperty(description = "Message to output when execution stops", exampleValue = "What just happened???")
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = StringUtils.trimToNull(message);
    }

    @Override
    public Statement[] generateStatements(ExecutionEnvironment env) {
        Action action = new ExecuteAction() {
            @Override
            public ExecuteResult execute(ExecutionEnvironment env) throws DatabaseException {
                throw new StopChangeException(getMessage());
            }

            @Override
            public String describe() {
                return "Stop Execution";
            }
        };

        return new Statement[] { new RawActionStatement(action)};
    }

    @Override
    public String getConfirmationMessage() {
        return "Changelog Execution Stopped";
    }

    public static class StopChangeException extends RuntimeException {
        public StopChangeException(String message) {
            super(message);
        }
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

    @Override
    protected void customLoadLogic(ParsedNode parsedNode, ResourceAccessor resourceAccessor) throws ParsedNodeException {
        Object value = parsedNode.getValue();
        if (value != null && value instanceof String) {
            setMessage((String) value);
        }
    }
}
