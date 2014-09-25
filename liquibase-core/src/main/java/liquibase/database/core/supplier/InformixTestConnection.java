package liquibase.database.core.supplier;

import liquibase.database.Database;
import liquibase.database.core.InformixDatabase;
import liquibase.sdk.supplier.database.JdbcTestConnection;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class InformixTestConnection extends JdbcTestConnection {

    @Override
    protected String getUrl() {
        return "jdbc:informix-sqli://" + getIpAddress() + ":9088/liquibase:informixserver=ol_ids_1150_1";
    }

    @Override
    public Database getCorrectDatabase() {
        return new InformixDatabase();
    }


    @Override
    public String describe() {
        return "Standard Informix connection";
    }

    @Override
    protected boolean sqlExecuteOnUnavailableConnection(String sql, Throwable openException) throws SQLException {
        if (sql.equals("EXECUTE PROCEDURE IFX_ALLOW_NEWLINE('T');")) {
            return true;
        }
        return super.sqlExecuteOnUnavailableConnection(sql, openException);
    }
}
