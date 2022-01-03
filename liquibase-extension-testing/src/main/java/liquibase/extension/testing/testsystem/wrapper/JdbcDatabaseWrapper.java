package liquibase.extension.testing.testsystem.wrapper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class JdbcDatabaseWrapper extends DatabaseWrapper {

    private final String url;
    private final String username;
    private final String password;
    private final Connection connection;

    public JdbcDatabaseWrapper(String url, String username, String password) throws SQLException {
        this.url = url;
        this.username = username;
        this.password = password;

        this.connection = DriverManager.getConnection(getUrl(), getUsername(), getPassword());
    }

    @Override
    public void start(boolean keepRunning) throws Exception {

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
