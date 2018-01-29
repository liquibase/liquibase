package liquibase.diff;

import liquibase.database.Database;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.compare.DatabaseObjectComparatorFactory;
import liquibase.exception.DatabaseException;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Catalog;
import liquibase.structure.core.Column;
import liquibase.structure.core.Schema;

import java.io.IOException;
import java.util.*;

public class DiffResult {

    private DatabaseSnapshot referenceSnapshot;
    private DatabaseSnapshot comparisonSnapshot;

    private CompareControl compareControl;

    private StringDiff productNameDiff;
    private StringDiff productVersionDiff;

    private Set<DatabaseObject> missingObjects = new HashSet<>();
    private Set<DatabaseObject> unexpectedObjects = new HashSet<>();
    private Map<DatabaseObject, ObjectDifferences> changedObjects = new HashMap<>();


    public DiffResult(DatabaseSnapshot referenceDatabaseSnapshot, DatabaseSnapshot comparisonDatabaseSnapshot, CompareControl compareControl) {
        this.referenceSnapshot = referenceDatabaseSnapshot;
        this.comparisonSnapshot = comparisonDatabaseSnapshot;
        this.compareControl = compareControl;
    }

    public DatabaseSnapshot getReferenceSnapshot() {
        return referenceSnapshot;
    }

    public DatabaseSnapshot getComparisonSnapshot() {
        return comparisonSnapshot;
    }

    public StringDiff getProductNameDiff() {
        return productNameDiff;
    }

    public void setProductNameDiff(StringDiff productNameDiff) {
        this.productNameDiff = productNameDiff;
    }

    public StringDiff getProductVersionDiff() {
        return productVersionDiff;
    }


    public void setProductVersionDiff(StringDiff productVersionDiff) {
        this.productVersionDiff = productVersionDiff;
    }

    public CompareControl getCompareControl() {
        return compareControl;
    }

    public Set<? extends DatabaseObject> getMissingObjects() {
        return missingObjects;
    }

    public <T extends DatabaseObject> Set<T> getMissingObjects(Class<T> type) {
        Set returnSet = new HashSet();
        for (DatabaseObject obj : missingObjects) {
            if (type.isAssignableFrom(obj.getClass())) {
                returnSet.add(obj);
            }
        }
        return returnSet;
    }

    public <T extends DatabaseObject> SortedSet<T> getMissingObjects(Class<T> type, Comparator<DatabaseObject> comparator) {
        TreeSet<T> set = new TreeSet<>(comparator);
        set.addAll(getMissingObjects(type));
        return set;
    }

    public <T extends DatabaseObject> T getMissingObject(T example, CompareControl.SchemaComparison[] schemaComparisons) {
        Database accordingTo = getComparisonSnapshot().getDatabase();
        DatabaseObjectComparatorFactory comparator = DatabaseObjectComparatorFactory.getInstance();
        for (T obj : (Set<T>) getMissingObjects(example.getClass())) {
            if (comparator.isSameObject(obj, example, schemaComparisons, accordingTo)) {
                return obj;
            }
        }
        return null;
    }

    public void addMissingObject(DatabaseObject obj) {
        if ((obj instanceof Column) && (((Column) obj).getComputed() != null) && ((Column) obj).getComputed()) {
            return; //not really missing, it's a virtual column
        }
        missingObjects.add(obj);
    }

    public Set<? extends DatabaseObject> getUnexpectedObjects() {
        return unexpectedObjects;
    }

    public <T extends DatabaseObject> Set<T> getUnexpectedObjects(Class<T> type) {
        Set returnSet = new HashSet();
        for (DatabaseObject obj : unexpectedObjects) {
            if (type.isAssignableFrom(obj.getClass())) {
                returnSet.add(obj);
            }
        }
        return returnSet;
    }

    public <T extends DatabaseObject> SortedSet<T> getUnexpectedObjects(Class<T> type, Comparator<DatabaseObject> comparator) {
        TreeSet<T> set = new TreeSet<>(comparator);
        set.addAll(getUnexpectedObjects(type));
        return set;
    }

    public <T extends DatabaseObject> T getUnexpectedObject(T example, CompareControl.SchemaComparison[] schemaComparisons) {
        Database accordingTo = this.getComparisonSnapshot().getDatabase();
        DatabaseObjectComparatorFactory comparator = DatabaseObjectComparatorFactory.getInstance();
        for (T obj : (Set<T>) getUnexpectedObjects(example.getClass())) {
            if (comparator.isSameObject(obj, example, schemaComparisons, accordingTo)) {
                return obj;
            }
        }
        return null;
    }

    public void addUnexpectedObject(DatabaseObject obj) {
        unexpectedObjects.add(obj);
    }

    public Map<DatabaseObject, ObjectDifferences> getChangedObjects() {
        return changedObjects;
    }

    public <T extends DatabaseObject> Map<T, ObjectDifferences> getChangedObjects(Class<T> type) {
        Map returnSet = new HashMap();
        for (Map.Entry<DatabaseObject, ObjectDifferences> obj : changedObjects.entrySet()) {
            if (type.isAssignableFrom(obj.getKey().getClass())) {
                returnSet.put(obj.getKey(), obj.getValue());
            }
        }
        return returnSet;
    }

    public <T extends DatabaseObject> SortedMap<T, ObjectDifferences> getChangedObjects(Class<T> type, Comparator<DatabaseObject> comparator) {
        SortedMap<T, ObjectDifferences> map = new TreeMap<>(comparator);
        map.putAll(getChangedObjects(type));
        return map;
    }

    public ObjectDifferences getChangedObject(DatabaseObject example, CompareControl.SchemaComparison[] schemaComparisons) {
        Database accordingTo = this.getComparisonSnapshot().getDatabase();
        DatabaseObjectComparatorFactory comparator = DatabaseObjectComparatorFactory.getInstance();
        for (Map.Entry<? extends DatabaseObject, ObjectDifferences> entry : getChangedObjects(example.getClass()).entrySet()) {
            if (comparator.isSameObject(entry.getKey(), example, schemaComparisons, accordingTo)) {
                return entry.getValue();
            }
        }
        return null;
    }


    public void addChangedObject(DatabaseObject obj, ObjectDifferences differences) {
        if ((obj instanceof Catalog) || (obj instanceof Schema)) {
            if ((differences.getSchemaComparisons() != null) && (differences.getDifferences().size() == 1) &&
                (differences.getDifference("name") != null)) {
                if ((obj instanceof Catalog) && this.getReferenceSnapshot().getDatabase().supportsSchemas()) { //still save name
                    changedObjects.put(obj, differences);
                    return;
                } else {
                    return;  //don't save name differences
                }
            }
        }
        changedObjects.put(obj, differences);
    }

    public boolean areEqual() throws DatabaseException, IOException {
//        boolean differencesInData = false;
//        if (compareControl.shouldDiffData()) {
//            List<ChangeSet> changeSets = new ArrayList<ChangeSet>();
//            addInsertDataChanges(changeSets, dataDir);
//            differencesInData = !changeSets.isEmpty();
//        }

        return missingObjects.isEmpty() && unexpectedObjects.isEmpty() && changedObjects.isEmpty();
    }

    public Set<Class<? extends DatabaseObject>> getComparedTypes() {
        return compareControl.getComparedTypes();
    }
}