package liquibase.statement;

import lombok.Getter;
import lombok.Setter;

@Getter
public class DatabaseFunction {

    /**
     * String value used for comparison. If a function matches this value then it should be replaced by the
     * real current timestamp function.
     */
    public static final String CURRENT_DATE_TIME_PLACE_HOLDER = "current_datetime";

    @Setter
    private String schemaName;

    private final String value;

    public DatabaseFunction(String value) {
        this.value = value;
    }

    public DatabaseFunction(String schemaName, String value) {
        this.schemaName = schemaName;
        this.value = value;
    }

    @Override
    public String toString() {
        return (getSchemaName() == null ? "" : getSchemaName() + ".") + getValue();
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
