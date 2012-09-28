package liquibase.snapshot;

import liquibase.database.Database;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.*;

import java.lang.reflect.Field;
import java.util.*;

public class DatabaseSnapshot {

    private SnapshotControl snapshotControl;
    private Database database;
    private Set<Schema> schemas = new HashSet<Schema>();

    public DatabaseSnapshot(Database database, SnapshotControl snapshotControl) {
        this.database = database;
        this.snapshotControl = snapshotControl;
    }

    public SnapshotControl getSnapshotControl() {
        return snapshotControl;
    }

    public Database getDatabase() {
        return database;
    }

    public Set<Schema> getSchemas() {
        return Collections.unmodifiableSet(schemas);
    }

    public void addSchema(Schema schema) {
        schemas.add(schema);
    }

    public boolean hasDatabaseChangeLogTable() {
        return false;  //todo
    }

}
