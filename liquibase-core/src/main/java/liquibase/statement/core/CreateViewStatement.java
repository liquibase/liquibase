package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Relation;
import liquibase.structure.core.View;

public class CreateViewStatement extends AbstractSqlStatement {

    private String catalogName;
    private String schemaName;
    private String viewName;
    private String selectQuery;
    private boolean replaceIfExists;

    public CreateViewStatement(String catalogName, String schemaName, String viewName, String selectQuery, boolean replaceIfExists) {
        this.catalogName = catalogName;
        this.schemaName = schemaName;
        this.viewName = viewName;
        this.selectQuery = selectQuery;
        this.replaceIfExists = replaceIfExists;
    }

    public String getCatalogName() {
        return catalogName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public String getViewName() {
        return viewName;
    }

    public String getSelectQuery() {
        return selectQuery;
    }

    public boolean isReplaceIfExists() {
        return replaceIfExists;
    }

    @Override
    protected DatabaseObject[] getBaseAffectedDatabaseObjects() {
        return new DatabaseObject[] {
            new View().setName(getViewName()).setSchema(getCatalogName(), getSchemaName())
        };
    }
}
