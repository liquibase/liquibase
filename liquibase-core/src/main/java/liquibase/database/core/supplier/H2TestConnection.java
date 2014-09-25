package liquibase.database.core.supplier;

import liquibase.database.Database;
import liquibase.database.core.H2Database;
import liquibase.sdk.supplier.database.JdbcTestConnection;
import liquibase.util.JdbcUtils;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

public class H2TestConnection extends JdbcTestConnection {


    @Override
    protected String getUrl() {
        return "jdbc:h2:mem:lbcat";
    }

    @Override
    protected Connection openConnection() throws Exception {
        Connection connection = super.openConnection();
        Statement statement = connection.createStatement();
        statement.execute("CREATE SCHEMA IF NOT EXISTS LBSCHEMA");
        statement.execute("CREATE SCHEMA IF NOT EXISTS LBSCHEMA2");
        JdbcUtils.closeStatement(statement);
        return connection;
    }


    @Override
    public Database getCorrectDatabase() {
        return new H2Database();
    }


    @Override
    public String describe() {
        return "Standard H2 connection";
    }

}
