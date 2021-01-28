package liquibase.configuration;

public class CurrentValueDetails<DataType> {
    DataType value;
    boolean wasOverridden;
    String sourceDescription;

    public DataType getValue() {
        return value;
    }

    public boolean getWasOverridden() {
        return wasOverridden;
    }

    public String getSourceDescription() {
        return sourceDescription;
    }
}
