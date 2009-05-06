package liquibase.statement;

public class DropViewStatement implements SqlStatement {

    private String schemaName;
    private String viewName;

    public DropViewStatement(String schemaName, String viewName) {
        this.schemaName = schemaName;
        this.viewName = viewName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public String getViewName() {
        return viewName;
    }
}
