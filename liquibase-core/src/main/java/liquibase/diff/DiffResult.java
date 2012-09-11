package liquibase.diff;

import liquibase.exception.DatabaseException;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.structure.DatabaseObject;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class DiffResult {

    private DatabaseSnapshot referenceSnapshot;
    private DatabaseSnapshot comparisonSnapshot;

    private StringDiff productName;
    private StringDiff productVersion;

    private Map<Class<? extends DatabaseObject>, DatabaseObjectDiff> databaseObjectDiffs = new HashMap<Class<? extends DatabaseObject>, DatabaseObjectDiff> ();

    private DataDiff data = new DataDiff();

    private DiffControl diffControl;

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

    public StringDiff getProductName() {
        return productName;
    }

    public void setProductName(StringDiff productName) {
        this.productName = productName;
    }

    public StringDiff getProductVersion() {
        return productVersion;
    }


    public void setProductVersion(StringDiff productVersion) {
        this.productVersion = productVersion;
    }

    public DiffControl getDiffControl() {
        return diffControl;
    }

    public <T extends DatabaseObject> DatabaseObjectDiff<T> getObjectDiff(Class<T> type) {
        if (!databaseObjectDiffs.containsKey(type)) {
            databaseObjectDiffs.put(type, new DatabaseObjectDiff());
        }
        return databaseObjectDiffs.get(type);
    }
    
    public DataDiff getData() {
        return data;
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

        if (!data.isEqual()) {
            return false;
        }

        return true;
    }
}