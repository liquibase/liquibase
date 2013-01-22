package liquibase.diff.compare.core;

import liquibase.database.Database;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.compare.DatabaseObjectComparator;
import liquibase.diff.compare.DatabaseObjectComparatorChain;
import liquibase.diff.compare.DatabaseObjectComparatorFactory;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.PrimaryKey;

public class PrimaryKeyComparator implements DatabaseObjectComparator {
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (PrimaryKey.class.isAssignableFrom(objectType)) {
            return PRIORITY_TYPE;
        }
        return PRIORITY_NONE;
    }

    public boolean isSameObject(DatabaseObject databaseObject1, DatabaseObject databaseObject2, Database accordingTo, DatabaseObjectComparatorChain chain) {
        if (!(databaseObject1 instanceof PrimaryKey && databaseObject2 instanceof PrimaryKey)) {
            return false;
        }

        PrimaryKey thisPrimaryKey = (PrimaryKey) databaseObject1;
        PrimaryKey otherPrimaryKey = (PrimaryKey) databaseObject2;

        return DatabaseObjectComparatorFactory.getInstance().isSameObject(thisPrimaryKey.getTable(), otherPrimaryKey.getTable(), accordingTo);
    }


    public ObjectDifferences findDifferences(DatabaseObject databaseObject1, DatabaseObject databaseObject2, Database accordingTo, DatabaseObjectComparatorChain chain) {
        ObjectDifferences differences = chain.findDifferences(databaseObject1, databaseObject2, accordingTo);
        return differences;
    }
}