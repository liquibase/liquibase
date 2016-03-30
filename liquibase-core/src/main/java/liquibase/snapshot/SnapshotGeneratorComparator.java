package liquibase.snapshot;

import liquibase.database.Database;
import liquibase.structure.DatabaseObject;

import java.util.Comparator;

class SnapshotGeneratorComparator implements Comparator<SnapshotGenerator> {

    private Class<? extends DatabaseObject> objectType;
    private Database database;

    public SnapshotGeneratorComparator(Class<? extends DatabaseObject> objectType, Database database) {
        this.objectType = objectType;
        this.database = database;
    }

    @Override
    public int compare(SnapshotGenerator o1, SnapshotGenerator o2) {
        int result = -1 * Integer.valueOf(o1.getPriority(objectType, database)).compareTo(o2.getPriority(objectType, database));
        if (result == 0) {
            return o1.getClass().getName().compareTo(o2.getClass().getName());
        }
        return result;
    }
}
