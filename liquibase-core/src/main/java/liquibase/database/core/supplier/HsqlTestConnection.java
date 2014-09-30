package liquibase.database.core.supplier;

import liquibase.database.Database;
import liquibase.database.core.HsqlDatabase;
import liquibase.sdk.supplier.database.JdbcTestConnection;
import liquibase.util.JdbcUtils;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

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
        statement.execute("CREATE SCHEMA LBSCHEMA2");
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

    @Override
    public List<String> getAvailableCatalogs() {
        return Arrays.asList(new String[] {null, "PUBLIC"});
    }

    @Override
    public String getPrimaryCatalog() {
        return "PUBLIC";
    }
}
