package liquibase.extension.testing.testsystem.wrapper;

import liquibase.exception.UnexpectedLiquibaseException;

import java.sql.SQLException;

/**
 * Implementation of {@link DatabaseWrapper} for databases that are connected to via a JDBC url and are not "started" in a traditional sense.
 */
public class JdbcDatabaseWrapper extends DatabaseWrapper {

    private final String url;
    private final String username;
    private final String password;

    public JdbcDatabaseWrapper(String url, String username, String password) throws SQLException {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    @Override
    public String describe() {
        return "Wrapped URL: " + url + "\n";
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void stop() throws Exception {
        throw new UnexpectedLiquibaseException("Cannot stop externally-managed database " + url);
    }

    @Override
    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String getUrl() {
        return url;
    }
}
