package liquibase.diff.compare;

import liquibase.database.Database;
import liquibase.diff.ObjectDifferences;
import liquibase.structure.DatabaseObject;

import java.util.Iterator;
import java.util.SortedSet;

public class DatabaseObjectComparatorChain {
    private Iterator<DatabaseObjectComparator> comparators;

    public DatabaseObjectComparatorChain(SortedSet<DatabaseObjectComparator> comparators) {
        if (comparators != null) {
            this.comparators = comparators.iterator();
        }
    }

    public boolean isSameObject(DatabaseObject object1, DatabaseObject object2, Database accordingTo) {
        if (object1 == null && object2 == null) {
            return true;
        }
        if (object1 == null && object2 != null) {
            return false;
        }

        if (object1 != null && object2 == null) {
            return false;
        }

        if (comparators == null) {
            return true;
        }

        if (!comparators.hasNext()) {
            return true;
        }

        return comparators.next().isSameObject(object1, object2, accordingTo, this);
    }

    public ObjectDifferences findDifferences(DatabaseObject object1, DatabaseObject object2, Database accordingTo) {
        if (object1 == null && object2 == null) {
            return new ObjectDifferences();
        }
        if (object1 == null && object2 != null) {
            return new ObjectDifferences().addDifference("Reference value was null", "this", null, null);
        }

        if (object1 != null && object2 == null) {
            return new ObjectDifferences().addDifference("Compared value was null", "this", null, null);
        }

        if (comparators == null) {
            return new ObjectDifferences();
        }

        if (!comparators.hasNext()) {
            return new ObjectDifferences();
        }

        return comparators.next().findDifferences(object1, object2, accordingTo, this);
    }
}
