package liquibase.diff.compare.core;

import liquibase.database.Database;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.compare.DatabaseObjectComparator;
import liquibase.diff.compare.DatabaseObjectComparatorChain;
import liquibase.diff.compare.DatabaseObjectComparatorFactory;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;
import liquibase.structure.core.PrimaryKey;

import java.util.Set;

public class PrimaryKeyComparator implements DatabaseObjectComparator {
    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (PrimaryKey.class.isAssignableFrom(objectType)) {
            return PRIORITY_TYPE;
        }
        return PRIORITY_NONE;
    }


    @Override
    public String[] hash(DatabaseObject databaseObject, Database accordingTo, DatabaseObjectComparatorChain chain) {
        return DatabaseObjectComparatorFactory.getInstance().hash(((PrimaryKey) databaseObject).getTable(), accordingTo);
    }

    @Override
    public boolean isSameObject(DatabaseObject databaseObject1, DatabaseObject databaseObject2, Database accordingTo, DatabaseObjectComparatorChain chain) {
        if (!(databaseObject1 instanceof PrimaryKey && databaseObject2 instanceof PrimaryKey)) {
            return false;
        }

        PrimaryKey thisPrimaryKey = (PrimaryKey) databaseObject1;
        PrimaryKey otherPrimaryKey = (PrimaryKey) databaseObject2;

        return DatabaseObjectComparatorFactory.getInstance().isSameObject(thisPrimaryKey.getTable(), otherPrimaryKey.getTable(), accordingTo);
    }


    @Override
    public ObjectDifferences findDifferences(DatabaseObject databaseObject1, DatabaseObject databaseObject2, Database accordingTo, CompareControl compareControl, DatabaseObjectComparatorChain chain, Set<String> exclude) {
        exclude.add("name");
        exclude.add("backingIndex");
        exclude.add("columnNames");
        ObjectDifferences differences = chain.findDifferences(databaseObject1, databaseObject2, accordingTo, compareControl, exclude);

        differences.compare("columnNames", databaseObject1, databaseObject2, new ObjectDifferences.DatabaseObjectNameCompareFunction(Column.class, accordingTo));
        return differences;
    }
}