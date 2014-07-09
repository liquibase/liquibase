package liquibase.database.core.supplier;

import liquibase.database.Database;
import liquibase.database.core.SQLiteDatabase;
import liquibase.sdk.supplier.database.JdbcTestConnection;

public class SQLiteTestConnection extends JdbcTestConnection {

    @Override
    protected String getUrl() {
        return "jdbc:sqlite:sqlite/liquibase.db";
    }

    @Override
    public Database getCorrectDatabase() {
        return new SQLiteDatabase();
    }

    @Override
    public String describe() {
        return "Standard Sqlite connection";
    }

}
