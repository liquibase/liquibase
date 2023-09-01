package liquibase.statement;

public class DatabaseFunction {

    /**
     * String value used for comparison. If a function matches this value then it should be replaces by the
     * real current timestamp function.
     */
    public static final String CURRENT_DATE_TIME_PLACE_HOLDER = "current_datetime";

    private String schemaName;

    private final String value;

    public DatabaseFunction(String value) {
        this.value = value;
    }

    public DatabaseFunction(String schemaName, String value) {
        this.schemaName = schemaName;
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return (getSchemaName() == null ? "" : getSchemaName() + ".") + getValue();
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
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
