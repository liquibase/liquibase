package liquibase.database.core;

import liquibase.Scope;
import liquibase.database.DatabaseConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.executor.ExecutorService;
import liquibase.statement.core.RawParameterizedSqlStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Table;
import liquibase.util.JdbcUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CockroachDatabase extends PostgresDatabase {

    private Integer databaseMajorVersion;
    private Integer databaseMinorVersion;

    private static final String VERSION_NUMBER_REGEX = "v(\\d+)\\.(\\d+)\\.(\\d+)";
    private static final Pattern VERSION_NUMBER_PATTERN = Pattern.compile(VERSION_NUMBER_REGEX);

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
    public int getDatabaseMajorVersion() throws DatabaseException {
        if (this.databaseMajorVersion == null) {
            return 20;
        }
        return this.databaseMajorVersion;
    }

    @Override
    public int getDatabaseMinorVersion() throws DatabaseException {
        if (this.databaseMinorVersion == null) {
            return 0;
        }
        return this.databaseMinorVersion;
    }

    @Override
    public boolean isCorrectDatabaseImplementation(DatabaseConnection conn) throws DatabaseException {
        final String url = conn.getURL();
        if (url.startsWith("jdbc:postgres") || url.startsWith("postgres")) {
            if (conn instanceof JdbcConnection) {
                try (Statement stmt = ((JdbcConnection) conn).createStatement()) {
                    if (stmt != null) {
                        try (ResultSet rs = stmt.executeQuery("select version()")) {
                            if (rs.next()) {
                                return ((String) JdbcUtil.getResultSetValue(rs, 1)).startsWith("CockroachDB");
                            }
                        }
                    }
                } catch (SQLException throwables) {
                    return false;
                }
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

    @Override
    public void setConnection(DatabaseConnection conn) {
        super.setConnection(conn);
        this.databaseMajorVersion = null;
        this.databaseMinorVersion = null;

        if (conn instanceof JdbcConnection) {
            try {
                String version = Scope.getCurrentScope().getSingleton(ExecutorService.class).
                        getExecutor("jdbc", this).queryForObject(new RawParameterizedSqlStatement("SELECT version()"), String.class);

                final Matcher versionMatcher = VERSION_NUMBER_PATTERN.matcher(version);
                if (versionMatcher.find()) {
                    this.databaseMajorVersion = Integer.parseInt(versionMatcher.group(1));
                    this.databaseMinorVersion = Integer.parseInt(versionMatcher.group(2));
                }
            } catch (Throwable e) {
                Scope.getCurrentScope().getLog(getClass()).fine("Cannot determine cockroachdb version: " + e.getMessage(), e);
            }
        } else {
            Scope.getCurrentScope().getLog(getClass()).fine("Cannot determine cockroachdb version: cannot query database");
        }
    }

    @Override
    public boolean useSerialDatatypes() {
        try {
            return !(getDatabaseMajorVersion() > 21 || (getDatabaseMajorVersion() == 21 && getDatabaseMinorVersion() >= 2));
        } catch (DatabaseException e) {
            return true;
        }
    }

    @Override
    public boolean supportsCreateIfNotExists(Class<? extends DatabaseObject> type) {
        return type.isAssignableFrom(Table.class);
    }
}
