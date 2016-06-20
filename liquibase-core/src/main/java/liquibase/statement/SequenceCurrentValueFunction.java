package liquibase.statement;

/**
 * Represents a function for getting the current value from a sequence
 */
public class SequenceCurrentValueFunction extends DatabaseFunction {

    private String sequenceSchemaName;

    public SequenceCurrentValueFunction(String sequenceName) {
        super(sequenceName);
    }

    public String getSequenceSchemaName() {
        return sequenceSchemaName;
    }

    public void setSequenceSchemaName(String sequenceSchemaName) {
        this.sequenceSchemaName = sequenceSchemaName;
    }
}