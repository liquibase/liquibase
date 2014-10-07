package liquibase.diff.compare.core;

import liquibase.database.Database;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.compare.CompareControl;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;
import liquibase.structure.core.Index;
import liquibase.diff.compare.DatabaseObjectComparator;
import liquibase.diff.compare.DatabaseObjectComparatorChain;
import liquibase.diff.compare.DatabaseObjectComparatorFactory;
import liquibase.structure.core.Table;

import java.util.*;

public class IndexComparator implements DatabaseObjectComparator {
    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (Index.class.isAssignableFrom(objectType)) {
            return PRIORITY_TYPE;
        }
        return PRIORITY_NONE;
    }

    @Override
    public String[] hash(DatabaseObject databaseObject, Database accordingTo, DatabaseObjectComparatorChain chain) {
        List<String> hashes = new ArrayList<String>();
        if (databaseObject.getName() != null) {
            hashes.add(databaseObject.getName().toLowerCase());
        }

        Table table = ((Index) databaseObject).getTable();
        if (table != null) {
            hashes.addAll(Arrays.asList(DatabaseObjectComparatorFactory.getInstance().hash(table, accordingTo)));
        }

        return hashes.toArray(new String[hashes.size()]);
    }


    @Override
    public boolean isSameObject(DatabaseObject databaseObject1, DatabaseObject databaseObject2, Database accordingTo, DatabaseObjectComparatorChain chain) {
        if (!(databaseObject1 instanceof Index && databaseObject2 instanceof Index)) {
            return false;
        }

        Index thisIndex = (Index) databaseObject1;
        Index otherIndex = (Index) databaseObject2;

        int thisIndexSize = thisIndex.getColumns().size();
        int otherIndexSize = otherIndex.getColumns().size();

        if (thisIndexSize > 0 && otherIndexSize > 0 && thisIndexSize != otherIndexSize) {
            return false;
        }

        if (thisIndex.getTable() != null && otherIndex.getTable() != null && thisIndexSize > 0 && otherIndexSize > 0) {
            if (!DatabaseObjectComparatorFactory.getInstance().isSameObject(thisIndex.getTable(), otherIndex.getTable(), accordingTo)) {
                return false;
            }

            for (int i=0; i< otherIndexSize; i++) {
                if (! DatabaseObjectComparatorFactory.getInstance().isSameObject(thisIndex.getColumns().get(i), otherIndex.getColumns().get(i), accordingTo)) {
                    return false;
                }
            }

            return true;
        } else {
            if (!DefaultDatabaseObjectComparator.nameMatches(databaseObject1, databaseObject2, accordingTo)) {
                return false;
            }

            if (databaseObject1.getSchema() != null && databaseObject2.getSchema() != null) {
                return DatabaseObjectComparatorFactory.getInstance().isSameObject(databaseObject1.getSchema(), databaseObject2.getSchema(), accordingTo);
            } else {
                return true;
            }
        }

    }


    @Override
    public ObjectDifferences findDifferences(DatabaseObject databaseObject1, DatabaseObject databaseObject2, Database accordingTo, CompareControl compareControl, DatabaseObjectComparatorChain chain, Set<String> exclude) {
        exclude.add("name");
        exclude.add("columns");
        ObjectDifferences differences = chain.findDifferences(databaseObject1, databaseObject2, accordingTo, compareControl, exclude);

        differences.compare("columns", databaseObject1, databaseObject2, new ObjectDifferences.DatabaseObjectNameCompareFunction(Column.class, accordingTo));
        return differences;
    }
}
