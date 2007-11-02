package liquibase.database.sql;

import liquibase.database.Database;
import liquibase.exception.StatementNotSupportedOnDatabaseException;

public class CreateViewStatement implements SqlStatement {

    private String schemaName;
    private String viewName;
    private String selectQuery;

    public CreateViewStatement(String schemaName, String viewName, String selectQuery) {
        this.schemaName = schemaName;
        this.viewName = viewName;
        this.selectQuery = selectQuery;
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

    public String getSqlStatement(Database database) throws StatementNotSupportedOnDatabaseException {
        return "CREATE VIEW " + database.escapeViewName(getSchemaName(), getViewName()) + " AS " + getSelectQuery();
    }

    public String getEndDelimiter(Database database) {
        return ";";
    }

    public boolean supportsDatabase(Database database) {
        return true;
    }
}
