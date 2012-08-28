package liquibase.snapshot.jvm;

import liquibase.database.Database;
import liquibase.database.core.HsqlDatabase;
import liquibase.database.structure.Schema;

public class HsqlDatabaseSnapshotGenerator extends JdbcDatabaseSnapshotGenerator {
    public boolean supports(Database database) {
        return database instanceof HsqlDatabase;
    }

    public int getPriority(Database database) {
        return PRIORITY_DATABASE;
    }

    @Override
    protected String getJdbcCatalogName(Schema schema) {
        return null;
    }

}