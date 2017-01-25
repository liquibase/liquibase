package liquibase.statement;

/**
 * Represents a function for getting the next value from a sequence
 */
public class SequenceNextValueFunction extends DatabaseFunction {

    private String sequenceSchemaName;

    public SequenceNextValueFunction() {
        this("UNSET", null);
    }

    public SequenceNextValueFunction(String sequenceName) {
        this(sequenceName, null);
    }

    public SequenceNextValueFunction(String sequenceName, String schemaName) {
        super(sequenceName);
        this.sequenceSchemaName = schemaName;
    }

    public String getSequenceSchemaName() {
        return sequenceSchemaName;
    }

    public void setSequenceSchemaName(String sequenceSchemaName) {
        this.sequenceSchemaName = sequenceSchemaName;
    }
}