package liquibase.statement;

public class DatabaseFunction {

    /**
     * String value used for comparison. If a function matches this value then it should be replaces by the
     * real current timestamp function.
     */
    public static final String CURRENT_DATE_TIME_PLACE_HOLDER = "current_datetime";

    private String value;

    public DatabaseFunction(String value) {
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
        if (obj instanceof DatabaseFunction) {
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
