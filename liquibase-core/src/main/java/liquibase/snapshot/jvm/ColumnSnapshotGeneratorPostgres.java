package liquibase.snapshot.jvm;

import liquibase.database.Database;
import liquibase.database.core.PostgresDatabase;
import liquibase.snapshot.SnapshotGenerator;
import liquibase.structure.DatabaseObject;

/**
 * Implements the PostgreSQL-specific parts of column snapshotting.
 */
public class ColumnSnapshotGeneratorPostgres extends ColumnSnapshotGenerator {

    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (database instanceof PostgresDatabase)
            return PRIORITY_DATABASE;
        else
            return PRIORITY_NONE; // Other DB? Let the generic handler do it.
    }

    @Override
    public Class<? extends SnapshotGenerator>[] replaces() {
        return new Class[]{ColumnSnapshotGenerator.class};
    }

}
