package liquibase.statement.core;

import liquibase.statement.SqlStatement;

public class RenameViewStatement implements SqlStatement {

    private String schemaName;
    private String oldViewName;
    private String newViewName;

    public RenameViewStatement(String schemaName, String oldViewName, String newViewName) {
        this.schemaName = schemaName;
        this.oldViewName = oldViewName;
        this.newViewName = newViewName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public String getOldViewName() {
        return oldViewName;
    }

    public String getNewViewName() {
        return newViewName;
    }
}
