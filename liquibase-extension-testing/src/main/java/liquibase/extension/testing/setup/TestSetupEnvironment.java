package liquibase.extension.testing.setup;

import liquibase.extension.testing.testsystem.DatabaseTestSystem;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class TestSetupEnvironment {

    public final String username;
    public final String url;
    public final String password;
    public final Connection connection;

    public final Connection altConnection;
    public final String altUsername;
    public final String altUrl;
    public final String altPassword;

    public String errorMessage;

    public TestSetupEnvironment(DatabaseTestSystem testSystem, DatabaseTestSystem altSystem) throws SQLException {
        this.connection = testSystem.getConnection();
        this.url = testSystem.getConnectionUrl();
        this.username = testSystem.getUsername();
        this.password = testSystem.getPassword();

        this.altUrl = this.url.replace(testSystem.getCatalog(), testSystem.getAltCatalog());
        this.altUsername = this.username;
        this.altPassword = this.password;
        this.altConnection = DriverManager.getConnection(this.altUrl, this.altUsername, this.altPassword);

    }
}
