package liquibase.diff.compare.core;

import liquibase.database.Database;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.compare.DatabaseObjectComparator;
import liquibase.diff.compare.DatabaseObjectComparatorChain;
import liquibase.diff.compare.DatabaseObjectComparatorFactory;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;
import liquibase.structure.core.ForeignKey;
import liquibase.structure.core.Table;
import liquibase.util.StringUtil;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ForeignKeyComparator implements DatabaseObjectComparator {
    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (ForeignKey.class.isAssignableFrom(objectType)) {
            return PRIORITY_TYPE;
        }
        return PRIORITY_NONE;
    }


    @Override
    public String[] hash(DatabaseObject databaseObject, Database accordingTo, DatabaseObjectComparatorChain chain) {
        Set<String> hashes = new HashSet<>();
        hashes.addAll(Arrays.asList(DatabaseObjectComparatorFactory.getInstance().hash(((ForeignKey) databaseObject).getForeignKeyTable(), null, accordingTo)));
        hashes.addAll(Arrays.asList(chain.hash(databaseObject, accordingTo)));

        return hashes.toArray(new String[hashes.size()]);
    }


    @Override
    public boolean isSameObject(DatabaseObject databaseObject1, DatabaseObject databaseObject2, Database accordingTo, DatabaseObjectComparatorChain chain) {
        if (!((databaseObject1 instanceof ForeignKey) && (databaseObject2 instanceof ForeignKey))) {
            return false;
        }

        ForeignKey thisForeignKey = (ForeignKey) databaseObject1;
        ForeignKey otherForeignKey = (ForeignKey) databaseObject2;

        if ((thisForeignKey.getName() != null) && (otherForeignKey.getName() != null)) {
            if (chain.isSameObject(thisForeignKey, otherForeignKey, accordingTo)) {
                return true;
            }
        }

        if ((thisForeignKey.getPrimaryKeyTable() == null) || (thisForeignKey.getForeignKeyTable() == null) ||
            (otherForeignKey.getPrimaryKeyTable() == null) || (otherForeignKey.getForeignKeyTable() == null)) {
            return false; //tables not set, have to rely on name
        }

        if ((thisForeignKey.getForeignKeyColumns() != null) && (thisForeignKey.getPrimaryKeyColumns() != null) &&
            (otherForeignKey.getForeignKeyColumns() != null) && (otherForeignKey.getPrimaryKeyColumns() != null)) {
        boolean columnsTheSame;
        StringUtil.StringUtilFormatter formatter = new StringUtil.StringUtilFormatter<Column>() {
            @Override
            public String toString(Column obj) {
                return obj.toString(false);
            }
        };

        if (accordingTo.isCaseSensitive()) {
                columnsTheSame = StringUtil.join(thisForeignKey.getForeignKeyColumns(), ",", formatter).equals(StringUtil.join(otherForeignKey.getForeignKeyColumns(), ",", formatter)) &&
                        StringUtil.join(thisForeignKey.getPrimaryKeyColumns(), ",", formatter).equals(StringUtil.join(otherForeignKey.getPrimaryKeyColumns(), ",", formatter));
        } else {
                columnsTheSame = StringUtil.join(thisForeignKey.getForeignKeyColumns(), ",", formatter).equalsIgnoreCase(StringUtil.join(otherForeignKey.getForeignKeyColumns(), ",", formatter)) &&
                        StringUtil.join(thisForeignKey.getPrimaryKeyColumns(), ",", formatter).equalsIgnoreCase(StringUtil.join(otherForeignKey.getPrimaryKeyColumns(), ",", formatter));
        }

        return columnsTheSame &&
                DatabaseObjectComparatorFactory.getInstance().isSameObject(thisForeignKey.getForeignKeyTable(), otherForeignKey.getForeignKeyTable(), chain.getSchemaComparisons(), accordingTo) &&
                DatabaseObjectComparatorFactory.getInstance().isSameObject(thisForeignKey.getPrimaryKeyTable(), otherForeignKey.getPrimaryKeyTable(), chain.getSchemaComparisons(), accordingTo);

    }

        return false;
    }

    @Override
    public ObjectDifferences findDifferences(DatabaseObject databaseObject1, DatabaseObject databaseObject2, Database accordingTo, CompareControl compareControl, DatabaseObjectComparatorChain chain, Set<String> exclue) {
        exclue.add("name");
        exclue.add("backingIndex");
        exclue.add("foreignKeyColumns");
        exclue.add("primaryKeyColumns");
        exclue.add("foreignKeyTable");
        exclue.add("primaryKeyTable");

        ObjectDifferences differences = chain.findDifferences(databaseObject1, databaseObject2, accordingTo, compareControl, exclue);
        differences.compare("foreignKeyColumns", databaseObject1, databaseObject2, new ObjectDifferences.DatabaseObjectNameCompareFunction(Column.class, accordingTo));
        differences.compare("primaryKeyColumns", databaseObject1, databaseObject2, new ObjectDifferences.DatabaseObjectNameCompareFunction(Column.class, accordingTo));
        differences.compare("foreignKeyTable", databaseObject1, databaseObject2, new ObjectDifferences.DatabaseObjectNameCompareFunction(Table.class, accordingTo));
        differences.compare("primaryKeyTable", databaseObject1, databaseObject2, new ObjectDifferences.DatabaseObjectNameCompareFunction(Table.class, accordingTo));
        return differences;
    }
}
