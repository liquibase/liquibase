package liquibase.statement;

/**
 * Represents a function for getting the next value from a sequence
 */
public class SequenceNextValueFunction extends DatabaseSchemaBasedFunction {

    public SequenceNextValueFunction() {
        this("UNSET");
    }

    public SequenceNextValueFunction(String sequenceName) {
        super(sequenceName);
    }

    public SequenceNextValueFunction(String sequenceName, String sequenceSchemaName) {
        super(sequenceName);
        setSchemaName(sequenceSchemaName);
    }

    public String getSequenceSchemaName() {
        return this.getSchemaName();
    }
}