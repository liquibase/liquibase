package liquibase.database;

import liquibase.exception.JDBCException;
import liquibase.database.sql.SqlStatement;
import liquibase.database.sql.RawSqlStatement;

import java.sql.Connection;

public class FirebirdDatabase extends AbstractDatabase {

    public boolean isCorrectDatabaseImplementation(Connection conn) throws JDBCException {
        return getDatabaseProductName(conn).startsWith("Firebird");
    }

    public String getDefaultDriver(String url) {
        if (url.startsWith("jdbc:firebirdsql")) {
            return "org.firebirdsql.jdbc.FBDriver";
        }
        return null;
    }

    public String getProductName() {
        return "Firebird";
    }

    public String getTypeName() {
        return "firebird";
    }


    public boolean supportsSequences() {
        return true;
    }

    protected String getBooleanType() {
        return "SMALLINT";
    }

    protected String getCurrencyType() {
        return "DECIMAL(18, 4)";
    }

    protected String getUUIDType() {
        return "CHAR(36)";
    }

    protected String getClobType() {
        return "CLOB";
    }

    protected String getBlobType() {
        return "BLOB";
    }

    protected String getDateTimeType() {
        return "TIMESTAMP";
    }

    public boolean supportsInitiallyDeferrableColumns() {
        return true;
    }

    public String getCurrentDateTimeFunction() {
        return "CURRENT_TIMESTAMP";
    }

    public boolean supportsTablespaces() {
        return true;
    }


    public SqlStatement createFindSequencesSQL() throws JDBCException {
        return new RawSqlStatement("SELECT RDB$GENERATOR_NAME FROM RDB$GENERATORS WHERE RDB$SYSTEM_FLAG IS NULL OR RDB$SYSTEM_FLAG = 0");
    }
}
