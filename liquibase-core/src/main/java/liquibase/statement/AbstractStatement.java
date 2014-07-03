package liquibase.statement;

import liquibase.AbstractExtensibleObject;
import liquibase.structure.DatabaseObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Convenience method for {@link liquibase.statement.Statement} implementations. Normally classes should extend this class vs. implementing Statement directly.
 */
public abstract class AbstractStatement extends AbstractExtensibleObject implements Statement {

    protected AbstractStatement() {
        init();
    }

    /**
     * Called by the constructor for initialization that should occur in all constructors.
     */
    protected void init() {

    }

    /**
     * Default implementation returns false
     */
    @Override
    public boolean skipOnUnsupported() {
        return false;
    }

    /**
     * Default implementation calls {@link #getBaseAffectedDatabaseObjects()} and then uses the {@link liquibase.structure.DatabaseObject#getContainingObjects()} method to find
     * other objects to add to the return collection.
     */
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

    /**
     * Implementations should return "examples" of the objects directly affected by this Statement.
     * Used by {@link liquibase.statement.AbstractStatement#getAffectedDatabaseObjects()}
     */
    protected abstract DatabaseObject[] getBaseAffectedDatabaseObjects();

}
