package liquibase.statement;

/**
 * Represents a function for getting the current value from a sequence
 */
public class SequenceCurrentValueFunction extends DatabaseSchemaBasedFunction {

    public SequenceCurrentValueFunction(String sequenceName) {
        super(sequenceName);
    }

    public SequenceCurrentValueFunction(String sequenceName, String sequenceSchemaName) {
        super(sequenceName);
        setSchemaName(sequenceSchemaName);
    }

    public String getSequenceSchemaName() {
        return this.getSchemaName();
    }
}