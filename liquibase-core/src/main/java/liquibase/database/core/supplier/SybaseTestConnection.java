package liquibase.database.core.supplier;

import liquibase.database.Database;
import liquibase.database.core.SybaseDatabase;
import liquibase.sdk.supplier.database.JdbcTestConnection;
import liquibase.util.CollectionUtil;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SybaseTestConnection extends JdbcTestConnection {

    @Override
    protected String getUrl() {
        return "jdbc:sybase:Tds:"+ getIpAddress()+":5000/liquibase";
    }

    @Override
    public Database getCorrectDatabase() {
        return new SybaseDatabase();
    }

    @Override
    public String describe() {
        return "Standard Syabase connection";
    }

    @Override
    protected List<Map<String, ?>> sqlQueryOnUnavailableConnection(String sql, Throwable openException) throws SQLException {
        if (sql.equals("select user_name()")) {
            return CollectionUtil.createSingleItemList("user_name", getDatabaseUsername());
        }
        return super.sqlQueryOnUnavailableConnection(sql, openException);
    }
}
