package liquibase.database.statement;

import liquibase.database.Database;
import liquibase.exception.StatementNotSupportedOnDatabaseException;

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

    public String getSqlStatement(Database database) throws StatementNotSupportedOnDatabaseException {
        return "DROP VIEW " + database.escapeViewName(getSchemaName(), getViewName());
    }

    public String getEndDelimiter(Database database) {
        return ";";
    }

    public boolean supportsDatabase(Database datCabase) {
        return true;
    }
}
