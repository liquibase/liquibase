package liquibase.database.core.supplier;

import liquibase.database.Database;
import liquibase.database.core.OracleDatabase;
import liquibase.sdk.supplier.database.JdbcTestConnection;

import java.util.Properties;

public class OracleTestConnection extends JdbcTestConnection {

    @Override
    protected String getUrl() {
        return "jdbc:oracle:thin:@" + getIpAddress() + ":1521:"+getSid();
    }

    @Override
    public Database getCorrectDatabase() {
        return new OracleDatabase();
    }

    public String getSid() {
        return "lqbase";
    }

    @Override
    public String describe() {
        return "Standard Oracle connection";
    }

    @Override
    protected Properties getConnectionProperties() {
        Properties properties = super.getConnectionProperties();
        properties.put("oracle.net.CONNECT_TIMEOUT", "1000");
        return properties;
    }



}
