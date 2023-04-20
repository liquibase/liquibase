package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;

public class RenameViewStatement extends AbstractSqlStatement {

    private String catalogName;
    private String schemaName;
    private String oldViewName;
    private String newViewName;

    public RenameViewStatement(String catalogName, String schemaName, String oldViewName, String newViewName) {
        this.catalogName = catalogName;
        this.schemaName = schemaName;
        this.oldViewName = oldViewName;
        this.newViewName = newViewName;
    }


    public String getCatalogName() {
        return catalogName;
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
