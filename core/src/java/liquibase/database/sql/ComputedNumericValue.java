package liquibase.database.sql;

public class ComputedNumericValue extends Number {

    private String value;

    public ComputedNumericValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public String toString() {
        return getValue();
    }

    public int intValue() {
        throw new RuntimeException("Value computed by database");
    }

    public long longValue() {
        throw new RuntimeException("Value computed by database");
    }

    public float floatValue() {
        throw new RuntimeException("Value computed by database");
    }

    public double doubleValue() {
        throw new RuntimeException("Value computed by database");
    }
}
