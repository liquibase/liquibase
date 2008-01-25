package liquibase.database.sql;

import liquibase.database.*;
import liquibase.exception.StatementNotSupportedOnDatabaseException;

public class CreateViewStatement implements SqlStatement {

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

    public String getSqlStatement(Database database) throws StatementNotSupportedOnDatabaseException {
        String createClause;
        if (database instanceof HsqlDatabase
                || database instanceof DB2Database
                || database instanceof CacheDatabase
                || database instanceof MSSQLDatabase
                || database instanceof DerbyDatabase) {
            if (replaceIfExists) {
                throw new StatementNotSupportedOnDatabaseException("replaceIfExists not supported", this, database);
            }
        }
        

        if (database instanceof FirebirdDatabase) {
            if (replaceIfExists) {
                createClause = "RECREATE VIEW";
            } else {
                createClause = "RECREATE VIEW";
            }
        } else {
            createClause = "CREATE " + (replaceIfExists ? "OR REPLACE " : "") + "VIEW";
        }

        return createClause + " " + database.escapeViewName(getSchemaName(), getViewName()) + " AS " + getSelectQuery();
    }

    public String getEndDelimiter(Database database) {
        return ";";
    }

    public boolean supportsDatabase(Database database) {
        return true;
    }
}
