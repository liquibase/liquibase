package liquibase.snapshot;

import liquibase.ExecutionEnvironment;
import liquibase.structure.DatabaseObject;

import java.util.Comparator;

class SnapshotGeneratorComparator implements Comparator<SnapshotLookupLogic> {

    private Class<? extends DatabaseObject> objectType;
    private ExecutionEnvironment environment;

    public SnapshotGeneratorComparator(Class<? extends DatabaseObject> objectType, ExecutionEnvironment environment) {
        this.objectType = objectType;
        this.environment = environment;
    }

    @Override
    public int compare(SnapshotLookupLogic o1, SnapshotLookupLogic o2) {
        int result = -1 * new Integer(o1.getPriority(objectType, environment)).compareTo(o2.getPriority(objectType, environment));
        if (result == 0) {
            return o1.getClass().getName().compareTo(o2.getClass().getName());
        }
        return result;
    }
}
