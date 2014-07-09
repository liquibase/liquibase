package liquibase.database.core.supplier;

import liquibase.database.Database;
import liquibase.database.core.HsqlDatabase;
import liquibase.sdk.supplier.database.JdbcTestConnection;
import liquibase.util.JdbcUtils;

import java.sql.Connection;
import java.sql.Statement;

public class HsqlTestConnection extends JdbcTestConnection {

    @Override
    protected String getUrl() {
        return "jdbc:hsqldb:mem:lbcat";
    }

    @Override
    protected Connection openConnection() throws Exception {
        Connection connection = super.openConnection();
        Statement statement = connection.createStatement();
        statement.execute("CREATE SCHEMA LBSCHEMA");
        JdbcUtils.closeStatement(statement);
        return connection;
    }

    @Override
    public Database getCorrectDatabase() {
        return new HsqlDatabase();
    }

    @Override
    public String describe() {
        return "Standard Hsql connection";
    }

}
