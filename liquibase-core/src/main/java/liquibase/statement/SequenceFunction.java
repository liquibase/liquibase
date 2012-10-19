package liquibase.statement;

/**
 * Represents a function for getting the next value from a sequence
 */
public class SequenceFunction {

    private String value;

    public SequenceFunction(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return getValue();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SequenceFunction) {
            return this.toString().equals(obj.toString());
        } else {
            return super.equals(obj);
        }
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }

}
