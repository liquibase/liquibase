package liquibase.database.core.supplier;

import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.core.DerbyDatabase;
import liquibase.sdk.supplier.database.JdbcTestConnection;
import liquibase.util.JdbcUtils;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

public class DerbyTestConnection extends JdbcTestConnection {

    @Override
    protected String getUrl() {
        return "jdbc:derby:memory:liquibase;create=true";
    }

    @Override
    protected Connection openConnection() throws Exception {
        Connection connection = super.openConnection();
        Statement statement = connection.createStatement();
        statement.execute("CREATE SCHEMA LBCAT");
        JdbcUtils.closeStatement(statement);
        return connection;
    }

    @Override
    public Database getCorrectDatabase() {
        return new DerbyDatabase();
    }

    @Override
    public String describe() {
        return "Standard Derby connection";
    }

    @Override
    public List<String[]> getTestCatalogsAndSchemas() {
        return super.getTestCatalogsAndSchemas();
    }
}
