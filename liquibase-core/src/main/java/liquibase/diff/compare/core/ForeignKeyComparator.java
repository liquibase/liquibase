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
        if (!(databaseObject1 instanceof ForeignKey && databaseObject2 instanceof ForeignKey)) {
            return false;
        }

        ForeignKey thisKey = (ForeignKey) databaseObject1;
        ForeignKey otherKey = (ForeignKey) databaseObject2;

        if (!thisKey.getDeleteRule().equals(otherKey.getDeleteRule())) {
            return false;
        }
        if (!thisKey.getUpdateRule().equals(otherKey.getUpdateRule())) {
            return false;
        }
        if (thisKey.isDeferrable() != otherKey.isDeferrable()) {
            return false;
        }
        if (thisKey.isInitiallyDeferred() != otherKey.isInitiallyDeferred()) {
            return false;
        }

        if (!DatabaseObjectComparatorFactory.getInstance().isSameObject(thisKey.getSchema(), otherKey.getSchema(), accordingTo)) {
            return false;
        }

        if (!DatabaseObjectComparatorFactory.getInstance().isSameObject(thisKey.getPrimaryKeyTable(), otherKey.getPrimaryKeyTable(), accordingTo)) {
            return false;
        }
        if (!thisKey.getPrimaryKeyColumns().equalsIgnoreCase(otherKey.getPrimaryKeyColumns())) {
            return false;
        }

        if (!DatabaseObjectComparatorFactory.getInstance().isSameObject(thisKey.getForeignKeyTable(), otherKey.getForeignKeyTable(), accordingTo)) {
            return false;
        }
        if (!thisKey.getForeignKeyColumns().equalsIgnoreCase(otherKey.getForeignKeyColumns())) {
            return false;
        }

        return true;
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