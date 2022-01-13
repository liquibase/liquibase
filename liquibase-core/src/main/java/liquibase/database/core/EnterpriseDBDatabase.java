package liquibase.database.core;

import liquibase.database.DatabaseConnection;
import liquibase.exception.DatabaseException;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.RawSqlStatement;

public class EnterpriseDBDatabase extends PostgresDatabase {

    @Override
    public boolean isCorrectDatabaseImplementation(DatabaseConnection conn) throws DatabaseException {
        final String url = conn.getURL();
        return url.contains(":edb:") || (url.contains(":postgres:") && url.contains(":5444"));
    }

    @Override
    public String getShortName() {
        return "edb";
    }

    @Override
    public int getPriority() {
        return PRIORITY_DATABASE + 5;
    }


    @Override
    public String getDefaultDriver(String url) {
        if (url.startsWith("jdbc:edb:")) {
            return "com.edb.Driver";
        }
        return null;
    }

    @Override
    public Integer getDefaultPort() {
        return 5444;
    }
}
