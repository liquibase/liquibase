package liquibase.database.core.supplier;

import liquibase.database.Database;
import liquibase.database.core.MySQLDatabase;
import liquibase.sdk.supplier.database.JdbcTestConnection;

import java.util.Properties;

public class MySQLTestConnection extends JdbcTestConnection {

    @Override
    protected String getUrl() {
        return "jdbc:mysql://"+ getIpAddress() +"/"+getPrimaryCatalog();
    }

    @Override
    public Database getCorrectDatabase() {
        return new MySQLDatabase();
    }

    @Override
    public String describe() {
        return "Standard MySQL connection";
    }

    @Override
    protected Properties getConnectionProperties() {
        Properties properties = super.getConnectionProperties();
        properties.setProperty("connectTimeout", "1000");
        return properties;
    }
}
