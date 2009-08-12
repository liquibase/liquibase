package liquibase.database.structure.type;

public class DatabaseValue {
    private Object value;
    private DataType type;

    public DatabaseValue(DataType type, Object value) {
        this.type = type;
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    public DataType getType() {
        return type;
    }
}
