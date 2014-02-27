package liquibase.sdk.supplier.database;

import liquibase.database.Database;
import liquibase.database.DatabaseFactory;

import java.util.HashSet;
import java.util.Set;

public class DatabaseSupplier {

    public Set<Database> getAllDatabases() {
        return new HashSet<Database>(DatabaseFactory.getInstance().getImplementedDatabases());
    }
}
