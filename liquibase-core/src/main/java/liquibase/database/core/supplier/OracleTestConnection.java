package liquibase.database.core.supplier;

import liquibase.database.Database;
import liquibase.database.core.OracleDatabase;
import liquibase.sdk.exception.UnexpectedLiquibaseSdkException;
import liquibase.sdk.supplier.database.AbstractTestConnection;
import liquibase.sdk.supplier.database.JdbcTestConnection;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

public class OracleTestConnection extends JdbcTestConnection {

    @Override
    public boolean supports(Database database) {
        return database instanceof OracleDatabase;
    }

    @Override
    protected String getUrl() {
        return "jdbc:oracle:thin:@" + getIpAddress() + ":1521:"+getSid();
    }

    public String getSid() {
        return "lqbase";
    }
}
