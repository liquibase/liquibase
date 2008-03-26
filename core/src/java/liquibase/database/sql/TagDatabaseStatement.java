package liquibase.database.sql;

import liquibase.database.Database;
import liquibase.database.MySQLDatabase;
import liquibase.exception.StatementNotSupportedOnDatabaseException;

public class TagDatabaseStatement implements SqlStatement {

    private String tag;

    public TagDatabaseStatement(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }

    public String getSqlStatement(Database database) throws StatementNotSupportedOnDatabaseException {
        UpdateStatement statement = new UpdateStatement(database.getDefaultSchemaName(), database.getDatabaseChangeLogTableName());
        statement.addNewColumnValue("TAG", tag);
        if (database instanceof MySQLDatabase) {
            statement.setWhereClause("DATEEXECUTED = (SELECT MAX(DATEEXECUTED) FROM (SELECT DATEEXECUTED FROM " + database.escapeTableName(database.getDefaultSchemaName(), database.getDatabaseChangeLogTableName())+") AS X)");
        } else {
            statement.setWhereClause("DATEEXECUTED = (SELECT MAX(DATEEXECUTED) FROM " + database.escapeTableName(database.getDefaultSchemaName(), database.getDatabaseChangeLogTableName())+")");
        }

        return statement.getSqlStatement(database);

    }

    public String getEndDelimiter(Database database) {
        return ";";
    }

    public boolean supportsDatabase(Database database) {
        return true;
    }
}
