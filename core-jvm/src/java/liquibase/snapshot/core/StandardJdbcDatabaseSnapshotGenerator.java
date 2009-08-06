package liquibase.snapshot.core;

import liquibase.database.Database;

public class StandardJdbcDatabaseSnapshotGenerator extends JdbcDatabaseSnapshotGenerator {

    public boolean supports(Database database) {
        return true;
    }

    public int getPriority(Database database) {
        return PRIORITY_DEFAULT;
    }

}
