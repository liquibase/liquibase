package liquibase.statement;

public class ComputedNumericValue extends Number {

    private String value;

    public ComputedNumericValue(String value) {
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
    public int intValue() {
        throw new RuntimeException("Value computed by database");
    }

    @Override
    public long longValue() {
        throw new RuntimeException("Value computed by database");
    }

    @Override
    public float floatValue() {
        throw new RuntimeException("Value computed by database");
    }

    @Override
    public double doubleValue() {
        throw new RuntimeException("Value computed by database");
    }
}
