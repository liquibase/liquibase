package liquibase.statement;

/**
 * Represents a function for getting the current value from a sequence
 */
public class SequenceCurrentValueFunction extends DatabaseFunction {

    public SequenceCurrentValueFunction(String sequenceName) {
        super(sequenceName);
    }

    public SequenceCurrentValueFunction(String sequenceSchemaName, String sequenceName) {
        super(sequenceSchemaName, sequenceName);
    }

    /**
     * @deprecated use {@link #getSchemaName()}
     */
    public String getSequenceSchemaName() {
        return this.getSchemaName();
    }

    /**
     * @deprecated use {@link #getSchemaName()}
     */
    public void setSequenceSchemaName(String sequenceSchemaName) {
        super.setSchemaName(sequenceSchemaName);
    }
}
