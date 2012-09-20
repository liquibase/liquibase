package liquibase.snapshot.jvm;

import java.util.Comparator;

class DatabaseObjectGeneratorComparator implements Comparator<DatabaseObjectSnapshotGenerator> {
    public int compare(DatabaseObjectSnapshotGenerator o1, DatabaseObjectSnapshotGenerator o2) {
        return -1 * new Integer(o1.getPriority()).compareTo(o2.getPriority());
    }
}
