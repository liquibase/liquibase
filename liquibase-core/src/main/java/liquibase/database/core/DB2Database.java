package liquibase.database.core;

import liquibase.database.DatabaseConnection;
import liquibase.exception.DatabaseException;
import liquibase.util.StringUtil;

public class DB2Database extends AbstractDb2Database {

    @Override
    public boolean isCorrectDatabaseImplementation(DatabaseConnection conn) throws DatabaseException {
        return conn.getDatabaseProductName().startsWith("DB2") && !StringUtil.startsWith(conn.getDatabaseProductVersion(), "DSN");
    }

    @Override
    public String getShortName() {
        return "db2";
    }

}
