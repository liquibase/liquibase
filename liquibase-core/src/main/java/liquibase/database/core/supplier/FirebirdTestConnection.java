package liquibase.database.core.supplier;

import liquibase.database.Database;
import liquibase.database.core.FirebirdDatabase;
import liquibase.sdk.supplier.database.JdbcTestConnection;

public class FirebirdTestConnection extends JdbcTestConnection {

    @Override
    protected String getUrl() {
        return "jdbc:firebirdsql:"+ getIpAddress() +"/3050:c:\\firebird\\liquibase.fdb";
    }

    @Override
    public Database getCorrectDatabase() {
        return new FirebirdDatabase();
    }

    @Override
    public String describe() {
        return "Standard Firebird connection";
    }

}
