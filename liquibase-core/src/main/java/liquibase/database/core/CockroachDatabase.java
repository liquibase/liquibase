package liquibase.database.core;

import liquibase.database.DatabaseConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.util.JdbcUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class CockroachDatabase extends PostgresDatabase {

    public CockroachDatabase() {
        super.setCurrentDateTimeFunction("NOW()");
    }

    @Override
    public int getPriority() {
        return super.getPriority() + 5;
    }

    @Override
    public String getShortName() {
        return "cockroachdb";
    }

    @Override
    protected String getDefaultDatabaseProductName() {
        return "CockroachDB";
    }

    @Override
    public boolean isCorrectDatabaseImplementation(DatabaseConnection conn) throws DatabaseException {
        if (conn instanceof JdbcConnection) {
            try (Statement stmt = ((JdbcConnection) conn).createStatement()) {
                if (stmt != null) {
                    try (ResultSet rs = stmt.executeQuery("select version()")) {
                        if (rs.next()) {
                            return ((String) JdbcUtils.getResultSetValue(rs, 1)).startsWith("CockroachDB");
                        }
                    }
                }
            } catch (SQLException throwables) {
                return false;
            }
        }

        return false;
    }

    @Override
    public Integer getDefaultPort() {
        return 26257;
    }

    @Override
    public boolean supportsInitiallyDeferrableColumns() {
        return false;
    }

    @Override
    public boolean supportsTablespaces() {
        return false;
    }

    @Override
    public boolean supportsDDLInTransaction() {
        return false;
    }

}
