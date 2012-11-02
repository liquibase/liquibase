package liquibase.diff;

import liquibase.exception.DatabaseException;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.structure.DatabaseObject;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DiffResult {

    private DatabaseSnapshot referenceSnapshot;
    private DatabaseSnapshot comparisonSnapshot;

    private DiffControl diffControl;

    private StringDiff productNameDiff;
    private StringDiff productVersionDiff;

    private Map<Class<? extends DatabaseObject>, DatabaseObjectDiff> databaseObjectDiffs = new HashMap<Class<? extends DatabaseObject>, DatabaseObjectDiff> ();


    public DiffResult(DatabaseSnapshot referenceDatabaseSnapshot, DatabaseSnapshot comparisonDatabaseSnapshot, DiffControl diffControl) {
        this.referenceSnapshot = referenceDatabaseSnapshot;
        this.comparisonSnapshot = comparisonDatabaseSnapshot;
        this.diffControl = diffControl;
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

    public DiffControl getDiffControl() {
        return diffControl;
    }

    public Set<Class<? extends DatabaseObject>> getComparedTypes() {
        return databaseObjectDiffs.keySet();
    }

    public <T extends DatabaseObject> DatabaseObjectDiff<T> getObjectDiff(Class<T> type) {
        if (!databaseObjectDiffs.containsKey(type)) {
            databaseObjectDiffs.put(type, new DatabaseObjectDiff());
        }
        return databaseObjectDiffs.get(type);
    }
    
    public boolean areEqual() throws DatabaseException, IOException {
//        boolean differencesInData = false;
//        if (diffControl.shouldDiffData()) {
//            List<ChangeSet> changeSets = new ArrayList<ChangeSet>();
//            addInsertDataChanges(changeSets, dataDir);
//            differencesInData = !changeSets.isEmpty();
//        }

        for (DatabaseObjectDiff diff : databaseObjectDiffs.values()) {
            if (!diff.areEqual()) {
                return false;
            }
        }

        return true;
    }
}