package liquibase.sdk.supplier.database;

import liquibase.database.Database;

public abstract class AbstractTestConnection implements TestConnection {

    private Database database;

    @Override
    public void init(Database database) {
        this.database = database;
    }

    public Database getDatabase() {
        return database;
    }

    @Override
    public String toString() {
        return describe();
    }
}
