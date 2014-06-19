package liquibase.statement;

import liquibase.structure.DatabaseObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public abstract class AbstractSqlStatement implements SqlStatement {

    @Override
    public boolean skipOnUnsupported() {
        return false;
    }

    @Override
    public Collection<? extends DatabaseObject> getAffectedDatabaseObjects() {
        List<DatabaseObject> affectedDatabaseObjects = new ArrayList<DatabaseObject>();
        DatabaseObject[] baseAffectedDatabaseObjects = getBaseAffectedDatabaseObjects();
        if (baseAffectedDatabaseObjects != null) {
            affectedDatabaseObjects.addAll(Arrays.asList(baseAffectedDatabaseObjects));
            List<DatabaseObject> moreAffectedDatabaseObjects = new ArrayList<DatabaseObject>();

            boolean foundMore = true;
            while (foundMore) {
                for (DatabaseObject object : affectedDatabaseObjects) {
                    DatabaseObject[] containingObjects = object.getContainingObjects();
                    if (containingObjects != null) {
                        for (DatabaseObject containingObject : containingObjects) {
                            if (containingObject != null && !affectedDatabaseObjects.contains(containingObject) && !moreAffectedDatabaseObjects.contains(containingObject)) {
                                moreAffectedDatabaseObjects.add(containingObject);
                            }
                        }
                    }
                }
                foundMore = moreAffectedDatabaseObjects.size() > 0;
                affectedDatabaseObjects.addAll(moreAffectedDatabaseObjects);
                moreAffectedDatabaseObjects.clear();
            }

            affectedDatabaseObjects.addAll(moreAffectedDatabaseObjects);
        }

        return affectedDatabaseObjects;
    }

    protected abstract DatabaseObject[] getBaseAffectedDatabaseObjects();
}
