package liquibase.diff.compare.core;

import liquibase.database.Database;
import liquibase.diff.ObjectDifferences;
import liquibase.structure.DatabaseObject;
import liquibase.diff.compare.DatabaseObjectComparator;
import liquibase.diff.compare.DatabaseObjectComparatorChain;

public final class DefaultDatabaseObjectComparator implements DatabaseObjectComparator {
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        return PRIORITY_DEFAULT;
    }

    public boolean isSameObject(DatabaseObject databaseObject1, DatabaseObject databaseObject2, Database accordingTo, DatabaseObjectComparatorChain chain) {
        if (databaseObject1.getClass().isAssignableFrom(databaseObject2.getClass()) || databaseObject2.getClass().isAssignableFrom(databaseObject1.getClass())) {
            return nameMatches(databaseObject1, databaseObject2, accordingTo);
        }
        return false;

    }

    public ObjectDifferences findDifferences(DatabaseObject databaseObject1, DatabaseObject databaseObject2, Database accordingTo, DatabaseObjectComparatorChain chain) {
        String object1Name = accordingTo.correctObjectName(databaseObject1.getName(), databaseObject1.getClass());
        String object2Name = accordingTo.correctObjectName(databaseObject2.getName(), databaseObject2.getClass());

        ObjectDifferences differences = new ObjectDifferences();

//        if (!object1Name.equals(object2Name)) {
//            differences.addDifference("name", object1Name, object2Name);
//        }

        return differences;
    }

    protected boolean nameMatches(DatabaseObject databaseObject1, DatabaseObject databaseObject2, Database accordingTo) {
        String object1Name = accordingTo.correctObjectName(databaseObject1.getName(), databaseObject1.getClass());
        String object2Name = accordingTo.correctObjectName(databaseObject2.getName(), databaseObject2.getClass());

        if (object1Name == null || object2Name == null) {
            return false;
        }
        return object1Name.equals(object2Name);
    }

}
