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

    /**
     * Checks if the given connection points to a CockroachDB database.
     * <p>
     * Returns false early if the URL is null (which can happen with some JDBC drivers
     * like IBM Informix), since a null URL cannot match any database type.
     *
     * @param conn the database connection to check
     * @return true if this is a CockroachDB connection, false otherwise
     * @throws DatabaseException if there is an error querying the database
     */
    @Override
    public boolean isCorrectDatabaseImplementation(DatabaseConnection conn) throws DatabaseException {
        final String url = conn.getURL();
        if (url == null || (!url.startsWith("jdbc:postgres") && !url.startsWith("postgres"))) {
            return false;
        }
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

    /**
     * The standard composite-type snapshot generator reads composite types via {@code pg_type}/
     * {@code typrelid}, which CockroachDB does not populate for user-defined composite types, so it
     * must not be treated as real PostgreSQL for composite-type snapshotting. Opts down from
     * {@link PostgresDatabase}.
     */
    @Override
    public boolean supportsCompositeTypeSnapshot() {
        return false;
    }

    /**
     * CockroachDB does not expose the stored-logic catalog the standard generators rely on.
     * Opts down from {@link PostgresDatabase}.
     */
    @Override
    public boolean supportsStoredLogicSnapshot() {
        return false;
    }
}
