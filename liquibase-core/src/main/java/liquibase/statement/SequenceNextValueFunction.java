package liquibase.statement;

/**
 * Represents a function for getting the next value from a sequence
 */
public class SequenceNextValueFunction extends DatabaseFunction {

    private String sequenceSchemaName;

    public SequenceNextValueFunction() {
        this("UNSET");
    }

    public SequenceNextValueFunction(String sequenceName) {
        super(sequenceName);
    }

    public String getSequenceSchemaName() {
        return sequenceSchemaName;
    }

    public void setSequenceSchemaName(String sequenceSchemaName) {
        this.sequenceSchemaName = sequenceSchemaName;
    }
}