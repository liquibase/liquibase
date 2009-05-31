package liquibase.statement;

public class SelectSequencesStatement implements SqlStatement {
    private String schemaName;

    public SelectSequencesStatement(String schemaName) {
        this.schemaName = schemaName;
    }

    public String getSchemaName() {
        return schemaName;
    }
}
