package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.View;

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

    @Override
    protected DatabaseObject[] getBaseAffectedDatabaseObjects() {
        return new DatabaseObject[] {
            new View().setName(getNewViewName()).setSchema(getCatalogName(), getSchemaName()),
            new View().setName(getOldViewName()).setSchema(getCatalogName(), getSchemaName()),
        };
    }
}
