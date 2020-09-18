package liquibase.statement;

/**
 * Represents a function that may depend on a schema
 */
public class DatabaseSchemaBasedFunction extends DatabaseFunction {
    private String schemaName;

    public DatabaseSchemaBasedFunction(String value) {
        super(value);
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }
}
