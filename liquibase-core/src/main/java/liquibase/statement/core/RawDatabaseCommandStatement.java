package liquibase.statement.core;

import liquibase.statement.AbstractStatement;
import liquibase.structure.DatabaseObject;

public class RawDatabaseCommandStatement extends AbstractStatement {

    private String command;
    private String endDelimiter  = ";";


    public RawDatabaseCommandStatement(String command) {
        this.command = command;
    }

    public RawDatabaseCommandStatement(String command, String endDelimiter) {
        this(command);
        if (endDelimiter != null) {
            this.endDelimiter = endDelimiter;
        }
    }

    public String getCommand() {
        return command;
    }

    public String getEndDelimiter() {
        return endDelimiter.replace("\\r","\r").replace("\\n","\n");
    }

    @Override
    public String toString() {
        return command;
    }

    @Override
    protected DatabaseObject[] getBaseAffectedDatabaseObjects() {
        return null;
    }
}
