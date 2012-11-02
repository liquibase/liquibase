package liquibase.structurecompare;

import liquibase.database.Database;
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
}
