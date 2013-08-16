package liquibase.diff.compare.core;

import liquibase.database.Database;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.compare.DatabaseObjectComparator;
import liquibase.diff.compare.DatabaseObjectComparatorChain;
import liquibase.diff.compare.DatabaseObjectComparatorFactory;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;

public class ColumnComparator implements DatabaseObjectComparator {
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (Column.class.isAssignableFrom(objectType)) {
            return PRIORITY_TYPE;
        }
        return PRIORITY_NONE;
    }

    public boolean isSameObject(DatabaseObject databaseObject1, DatabaseObject databaseObject2, Database accordingTo, DatabaseObjectComparatorChain chain) {
        if (!(databaseObject1 instanceof Column && databaseObject2 instanceof Column)) {
            return false;
        }

        Column thisColumn = (Column) databaseObject1;
        Column otherColumn = (Column) databaseObject2;

        if (!DatabaseObjectComparatorFactory.getInstance().isSameObject(thisColumn.getRelation(), otherColumn.getRelation(), accordingTo)) {
            return false;
        }

        return chain.isSameObject(databaseObject1, databaseObject2, accordingTo);
    }


    public ObjectDifferences findDifferences(DatabaseObject databaseObject1, DatabaseObject databaseObject2, Database accordingTo, DatabaseObjectComparatorChain chain) {
        ObjectDifferences differences = chain.findDifferences(databaseObject1, databaseObject2, accordingTo);
        differences.removeDifference("autoIncrementInformation");

        differences.removeDifference("defaultValue");
//        differences.compare("defaultValue", databaseObject1, databaseObject2, new ObjectDifferences.ToStringCompareFunction()); //often differences between what database reports even if they are the same

        return differences;
    }
}
