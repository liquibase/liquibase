package liquibase.database.sql;

import liquibase.database.Database;
import liquibase.database.MySQLDatabase;
import liquibase.exception.StatementNotSupportedOnDatabaseException;
import liquibase.exception.JDBCException;

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
            try {
                long version = Long.parseLong(database.getDatabaseProductVersion().substring(0,1));

                if (version < 5) {
                    return new RawSqlStatement("UPDATE DATABASECHANGELOG C LEFT JOIN (SELECT MAX(DATEEXECUTED) as MAXDATE FROM (SELECT DATEEXECUTED FROM`DATABASECHANGELOG`) AS X) D ON C.DATEEXECUTED = D.MAXDATE SET C.TAG = '"+tag+"'").getSqlStatement(database);
                }

            } catch (Throwable e) {
                ; //assume it is version 5
            }
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
