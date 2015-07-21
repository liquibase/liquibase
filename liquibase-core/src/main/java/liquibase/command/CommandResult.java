package liquibase.command;

import liquibase.AbstractExtensibleObject;

public class CommandResult extends AbstractExtensibleObject {

    public String message;
    public boolean succeeded;

    /**
     * Creates new CommandResult with succeeded=true and message="Successful"
     */
    public CommandResult() {
        this.message = "Successful";
        this.succeeded = true;
    }

    /**
     * Creates new CommandResult with the given message and succeeded=true
     */
    public CommandResult(String message) {
        this.message = message;
        this.succeeded = true;
    }

    public CommandResult(String message, boolean succeeded) {
        this.message = message;
        this.succeeded = succeeded;
    }
}
