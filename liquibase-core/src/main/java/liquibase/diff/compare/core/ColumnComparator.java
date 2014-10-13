package liquibase.diff.compare.core;

import liquibase.database.Database;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.compare.DatabaseObjectComparator;
import liquibase.diff.compare.DatabaseObjectComparatorChain;
import liquibase.diff.compare.DatabaseObjectComparatorFactory;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;

import java.util.Set;

public class ColumnComparator implements DatabaseObjectComparator {
    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (Column.class.isAssignableFrom(objectType)) {
            return PRIORITY_TYPE;
        }
        return PRIORITY_NONE;
    }

    @Override
    public String[] hash(DatabaseObject databaseObject, Database accordingTo, DatabaseObjectComparatorChain chain) {
        Column column = (Column) databaseObject;

        if (column.getRelation() == null) {
            return new String[] {(column.getName()).toLowerCase()};
        } else {
            return new String[] {(column.getRelation().getName() + ":" + column.getName()).toLowerCase()};
        }
    }

    @Override
    public boolean isSameObject(DatabaseObject databaseObject1, DatabaseObject databaseObject2, Database accordingTo, DatabaseObjectComparatorChain chain) {
        if (!(databaseObject1 instanceof Column && databaseObject2 instanceof Column)) {
            return false;
        }

        Column thisColumn = (Column) databaseObject1;
        Column otherColumn = (Column) databaseObject2;

        //short circut chain.isSameObject for performance reasons. There can be a lot of columns in a database
        if (!DefaultDatabaseObjectComparator.nameMatches(thisColumn, otherColumn, accordingTo)) {
            return false;
        }

        if (!DatabaseObjectComparatorFactory.getInstance().isSameObject(thisColumn.getRelation(), otherColumn.getRelation(), accordingTo)) {
            return false;
        }

        return true;
    }


    @Override
    public ObjectDifferences findDifferences(DatabaseObject databaseObject1, DatabaseObject databaseObject2, Database accordingTo, CompareControl compareControl, DatabaseObjectComparatorChain chain, Set<String> exclude) {
        exclude.add("name");
        exclude.add("type");
        ObjectDifferences differences = chain.findDifferences(databaseObject1, databaseObject2, accordingTo, compareControl, exclude);

        differences.compare("name", databaseObject1, databaseObject2, new ObjectDifferences.DatabaseObjectNameCompareFunction(Column.class, accordingTo));
        differences.compare("type", databaseObject1, databaseObject2, new ObjectDifferences.DatabaseObjectNameCompareFunction(Column.class, accordingTo));

        return differences;
    }
}
