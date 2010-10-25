package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;

public class CreateViewStatement extends AbstractSqlStatement {

    private String schemaName;
    private String viewName;
    private String selectQuery;
    private boolean replaceIfExists;

    public CreateViewStatement(String schemaName, String viewName, String selectQuery, boolean replaceIfExists) {
        this.schemaName = schemaName;
        this.viewName = viewName;
        this.selectQuery = selectQuery;
        this.replaceIfExists = replaceIfExists;
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

}
