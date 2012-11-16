package liquibase.diff.output.changelog;

import liquibase.change.Change;
import liquibase.structure.DatabaseObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ChangesNeeded {
    private Map<DatabaseObject, Change[]> changeForMissingObjects = new HashMap<DatabaseObject, Change[]>();
    private Map<DatabaseObject, Change[]> changeForUnexpectedObjects = new HashMap<DatabaseObject, Change[]>();
    private Map<DatabaseObject, Change[]> changeForChangedObjects = new HashMap<DatabaseObject, Change[]>();

    public void addChangeForMissingObject(DatabaseObject object, Change[] changes) {
        changeForMissingObjects.put(object, changes);
    }

    public void addChangeForUnexpectedObject(DatabaseObject object, Change[] changes) {
        changeForUnexpectedObjects.put(object, changes);
    }

    public void addChangeForChangedObject(DatabaseObject object, Change[] changes) {
        changeForChangedObjects.put(object, changes);
    }
}
