package liquibase.sdk.supplier.database;

import liquibase.database.Database;
import liquibase.database.DatabaseFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class DatabaseSupplier {

    private HashSet<Database> allDatabases;

    public Set<Database> getAllDatabases() {
        if (allDatabases == null) {
            allDatabases = new HashSet<Database>(DatabaseFactory.getInstance().getImplementedDatabases());
        }
        return Collections.unmodifiableSet(allDatabases);
    }
}
