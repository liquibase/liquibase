package liquibase.database.jvm;

import liquibase.exception.DatabaseException;

import java.sql.Driver;
import java.util.Properties;

public class SnowflakeJdbcConnection extends JdbcConnection {

    public static final String APPLICATION_KEY = "application";
    public static final String LIQUIBASE_PRODUCT_NAME = "Liquibase";

    @Override
    public int getPriority() {
        return super.getPriority()+1;
    }

    @Override
    public void open(String url, Driver driverObject, Properties driverProperties) throws DatabaseException {
        //
        // Add the application name to the JDBC properties
        //
        driverProperties.put(APPLICATION_KEY, LIQUIBASE_PRODUCT_NAME);
        super.open(url, driverObject, driverProperties);
    }
}
