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
import liquibase.util.StringUtils;

import java.util.List;
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
        PrimaryKey pk = (PrimaryKey) databaseObject;
        if (databaseObject.getName() == null) {
            return DatabaseObjectComparatorFactory.getInstance().hash(pk.getTable(),chain.getSchemaComparisons(), accordingTo);
        } else {
            if ((pk.getTable() == null) || (pk.getTable().getName() == null)) {
                return new String[] {pk.getName().toLowerCase() };
            } else {
                return new String[] {pk.getName().toLowerCase(), pk.getTable().getName().toLowerCase()};
            }
        }
    }

    @Override
    public boolean isSameObject(DatabaseObject databaseObject1, DatabaseObject databaseObject2, Database accordingTo, DatabaseObjectComparatorChain chain) {
        if (!((databaseObject1 instanceof PrimaryKey) && (databaseObject2 instanceof PrimaryKey))) {
            return false;
        }

        PrimaryKey thisPrimaryKey = (PrimaryKey) databaseObject1;
        PrimaryKey otherPrimaryKey = (PrimaryKey) databaseObject2;

        if ((thisPrimaryKey.getTable() != null) && (thisPrimaryKey.getTable().getName() != null) && (otherPrimaryKey
            .getTable() != null) && (otherPrimaryKey.getTable().getName() != null)) {
            return DatabaseObjectComparatorFactory.getInstance().isSameObject(thisPrimaryKey.getTable(), otherPrimaryKey.getTable(), chain.getSchemaComparisons(), accordingTo);
        } else {
            return StringUtils.trimToEmpty(thisPrimaryKey.getName()).equalsIgnoreCase(otherPrimaryKey.getName());
        }
    }


    @Override
    public ObjectDifferences findDifferences(DatabaseObject databaseObject1, DatabaseObject databaseObject2, Database accordingTo, CompareControl compareControl, DatabaseObjectComparatorChain chain, Set<String> exclude) {
        exclude.add("name");
        exclude.add("backingIndex");
        exclude.add("columns");
        ObjectDifferences differences = chain.findDifferences(databaseObject1, databaseObject2, accordingTo, compareControl, exclude);

        differences.compare("columns", databaseObject1, databaseObject2, new ObjectDifferences.CompareFunction() {
            @Override
            public boolean areEqual(Object referenceValue, Object compareToValue) {
                List<Column> referenceList = (List) referenceValue;
                List<Column> compareList = (List) compareToValue;

                if (referenceList.size() != compareList.size()) {
                    return false;
                }
                for (int i=0; i<referenceList.size(); i++) {
                    if (!StringUtils.trimToEmpty((referenceList.get(i)).getName()).equalsIgnoreCase(StringUtils.trimToEmpty(compareList.get(i).getName()))) {
                        return false;
                    }
                }
                return true;
            }
        });

        return differences;
    }
}