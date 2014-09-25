package liquibase.database.core.supplier;

import liquibase.database.Database;
import liquibase.database.core.OracleDatabase;
import liquibase.sdk.supplier.database.JdbcTestConnection;
import liquibase.util.CollectionUtil;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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

    @Override
    public List<String> getAvailableCatalogs() {
        return Arrays.asList(new String[] {null, "LBUSER", "LBUSER2"});
    }

    @Override
    protected List<Map<String, ?>> sqlQueryOnUnavailableConnection(String sql, Throwable openException) throws SQLException {
        if (sql.equalsIgnoreCase("select sys_context( 'userenv', 'current_schema' ) from dual")) {
            return CollectionUtil.createSingleItemList("current_schema", getPrimarySchema());
        }

        return super.sqlQueryOnUnavailableConnection(sql, openException);
    }
}
