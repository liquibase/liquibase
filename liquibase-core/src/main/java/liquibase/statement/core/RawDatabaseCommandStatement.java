package liquibase.statement.core;

import liquibase.statement.AbstractStatement;
import liquibase.structure.DatabaseObject;

/**
 * Execute a (non-sql) command against the database. For sql statements, use {@link liquibase.statement.core.RawSqlStatement}.
 * End delimiter defaults to ";", reverts to ";" if set to null.
 */
public class RawDatabaseCommandStatement extends AbstractStatement {

    public static final String COMMAND = "command";
    public static final String END_DELIMITER = "endDelimiter";


    public RawDatabaseCommandStatement() {
    }

    public RawDatabaseCommandStatement(String command) {
        setCommand(command);
    }

    public RawDatabaseCommandStatement(String command, String endDelimiter) {
        this(command);
        if (endDelimiter != null) {
            setEndDelimiter(endDelimiter);
        }
    }

    public String getCommand() {
        return getAttribute(COMMAND, String.class);
    }

    public RawDatabaseCommandStatement setCommand(String command) {
        return (RawDatabaseCommandStatement) setAttribute(COMMAND, command);
    }

    /**
     * Returns end delimiter. Will convert "\\r" and "\\n" strings to \r and \n"
     */
    public String getEndDelimiter() {
        return getAttribute(END_DELIMITER, ";").replace("\\r","\r").replace("\\n","\n");
    }

    public RawDatabaseCommandStatement setEndDelimiter(String endDelimiter) {
        return (RawDatabaseCommandStatement) setAttribute(END_DELIMITER, endDelimiter);
    }

    @Override
    public String toString() {
        return getCommand();
    }

    @Override
    protected DatabaseObject[] getBaseAffectedDatabaseObjects() {
        return null;
    }
}
