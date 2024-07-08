package liquibase.statement;

/**
 * Represents a function for getting the next value from a sequence
 */
public class SequenceNextValueFunction extends DatabaseFunction {

    public SequenceNextValueFunction() {
        this("UNSET");
    }

    public SequenceNextValueFunction(String sequenceName) {
        super(sequenceName);
    }

    public SequenceNextValueFunction(String sequenceSchemaName, String sequenceName) {
        super(sequenceSchemaName, sequenceName);
    }

    /**
     * @deprecated use {@link #getSchemaName()}
     */
    public String getSequenceSchemaName() {
        return this.getSchemaName();
    }

    /**
     * @deprecated use {@link #setSchemaName(String)}
     */
    public void setSequenceSchemaName(String sequenceSchemaName) {
        super.setSchemaName(sequenceSchemaName);
    }
}
