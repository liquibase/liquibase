package liquibase.diff.compare.core;

import liquibase.database.Database;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.compare.DatabaseObjectComparator;
import liquibase.diff.compare.DatabaseObjectComparatorChain;
import liquibase.diff.compare.DatabaseObjectComparatorFactory;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;
import liquibase.structure.core.ForeignKey;

public class ForeignKeyComparator implements DatabaseObjectComparator {
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (ForeignKey.class.isAssignableFrom(objectType)) {
            return PRIORITY_TYPE;
        }
        return PRIORITY_NONE;
    }

    public boolean isSameObject(DatabaseObject databaseObject1, DatabaseObject databaseObject2, Database accordingTo, DatabaseObjectComparatorChain chain) {
        return chain.isSameObject(databaseObject1, databaseObject2, accordingTo);
    }


    public ObjectDifferences findDifferences(DatabaseObject databaseObject1, DatabaseObject databaseObject2, Database accordingTo, DatabaseObjectComparatorChain chain) {
        ObjectDifferences differences = chain.findDifferences(databaseObject1, databaseObject2, accordingTo);
        differences.removeDifference("name");
        differences.removeDifference("backingIndex");

        differences.removeDifference("columnNames");
        differences.compare("columnNames", databaseObject1, databaseObject2, new ObjectDifferences.DatabaseObjectNameCompareFunction(Column.class, accordingTo));
        return differences;
    }
}