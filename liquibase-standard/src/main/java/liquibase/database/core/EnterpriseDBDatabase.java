package liquibase.database.core;

import liquibase.Scope;
import liquibase.database.DatabaseConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.structure.DatabaseObject;
import liquibase.util.JdbcUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class EnterpriseDBDatabase extends PostgresDatabase {

    @Override
    public boolean isCorrectDatabaseImplementation(DatabaseConnection conn) throws DatabaseException {
        final String url = conn.getURL();
        if (url.startsWith("jdbc:edb:") || url.startsWith("jdbc:postgres")) {
            if (conn instanceof JdbcConnection) {
                try (Statement stmt = ((JdbcConnection) conn).createStatement()) {
                    if (stmt != null) {
                        try (ResultSet rs = stmt.executeQuery("select version()")) {
                            if (rs.next()) {
                                return ((String) JdbcUtil.getResultSetValue(rs, 1)).contains("EnterpriseDB");
                            }
                        }
                    }
                } catch (SQLException e) {
                    Scope.getCurrentScope().getLog(getClass()).fine("Error checking if connection is an EnterpriseDB database: "+e.getMessage(), e);
                    return false;
                }
            }
        }

        return false;
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

    @Override
    public boolean supportsCreateIfNotExists(Class<? extends DatabaseObject> type) {
        return false;
    }
}
