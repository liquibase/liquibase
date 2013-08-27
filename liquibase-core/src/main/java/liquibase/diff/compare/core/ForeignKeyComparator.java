package liquibase.diff.compare.core;

import liquibase.database.Database;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.compare.DatabaseObjectComparator;
import liquibase.diff.compare.DatabaseObjectComparatorChain;
import liquibase.diff.compare.DatabaseObjectComparatorFactory;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;
import liquibase.structure.core.ForeignKey;
import liquibase.structure.core.Table;
import liquibase.util.StringUtils;

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

        ForeignKey thisForeignKey = (ForeignKey) databaseObject1;
        ForeignKey otherForeignKey = (ForeignKey) databaseObject2;

        if (thisForeignKey.getName() != null && databaseObject2.getName() != null) {
            if (chain.isSameObject(thisForeignKey, databaseObject2, accordingTo)) {
                return true;
            }
        }

        boolean columnsTheSame;
        if (accordingTo.isCaseSensitive()) {
            columnsTheSame = StringUtils.trimToEmpty(((ForeignKey) databaseObject1).getForeignKeyColumns()).equals(StringUtils.trimToEmpty(((ForeignKey) databaseObject2).getForeignKeyColumns())) &&
                    StringUtils.trimToEmpty(((ForeignKey) databaseObject1).getPrimaryKeyColumns()).equals(StringUtils.trimToEmpty(((ForeignKey) databaseObject2).getPrimaryKeyColumns()));
        } else {
            columnsTheSame = ((ForeignKey) databaseObject1).getForeignKeyColumns().equalsIgnoreCase(((ForeignKey) databaseObject2).getForeignKeyColumns()) &&
                    ((ForeignKey) databaseObject1).getPrimaryKeyColumns().equalsIgnoreCase(((ForeignKey) databaseObject2).getPrimaryKeyColumns());

        }

        return columnsTheSame &&
                DatabaseObjectComparatorFactory.getInstance().isSameObject(thisForeignKey.getForeignKeyTable(), otherForeignKey.getForeignKeyTable(), accordingTo) &&
                DatabaseObjectComparatorFactory.getInstance().isSameObject(thisForeignKey.getPrimaryKeyTable(), otherForeignKey.getPrimaryKeyTable(), accordingTo);

    }


    public ObjectDifferences findDifferences(DatabaseObject databaseObject1, DatabaseObject databaseObject2, Database accordingTo, DatabaseObjectComparatorChain chain) {
        ObjectDifferences differences = chain.findDifferences(databaseObject1, databaseObject2, accordingTo);
        differences.removeDifference("name");
        differences.removeDifference("backingIndex");

        differences.removeDifference("columnNames");
        differences.compare("foreignKeyColumns", databaseObject1, databaseObject2, new ObjectDifferences.DatabaseObjectNameCompareFunction(Column.class, accordingTo));
        differences.compare("primaryKeyColumns", databaseObject1, databaseObject2, new ObjectDifferences.DatabaseObjectNameCompareFunction(Column.class, accordingTo));
        differences.compare("foreignKeyTable", databaseObject1, databaseObject2, new ObjectDifferences.DatabaseObjectNameCompareFunction(Table.class, accordingTo));
        differences.compare("primaryKeyTable", databaseObject1, databaseObject2, new ObjectDifferences.DatabaseObjectNameCompareFunction(Table.class, accordingTo));
        return differences;
    }
}