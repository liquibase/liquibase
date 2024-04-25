package liquibase.diff.compare.core;

import liquibase.GlobalConfiguration;
import liquibase.database.Database;
import liquibase.database.core.PostgresDatabase;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.compare.DatabaseObjectComparator;
import liquibase.diff.compare.DatabaseObjectComparatorChain;
import liquibase.diff.compare.DatabaseObjectComparatorFactory;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;
import liquibase.util.BooleanUtil;

import java.util.Locale;
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

        String hash = column.getName();
        if (column.getRelation() != null) {
            hash += ":" + column.getRelation().getName();
        }
        if (BooleanUtil.isTrue(column.getComputed())) {
            hash += ":computed";
        }
        if (BooleanUtil.isTrue(column.getDescending())) {
            hash += ":descending";
        }
        return new String[] {hash.toLowerCase(Locale.US)};
    }

    @Override
    public boolean isSameObject(DatabaseObject databaseObject1, DatabaseObject databaseObject2, Database accordingTo, DatabaseObjectComparatorChain chain) {
        if (!((databaseObject1 instanceof Column) && (databaseObject2 instanceof Column))) {
            return false;
        }

        Column thisColumn = (Column) databaseObject1;
        Column otherColumn = (Column) databaseObject2;

        //short circuit chain.isSameObject for performance reasons. There can be a lot of columns in a database
        if (!DefaultDatabaseObjectComparator.nameMatches(thisColumn, otherColumn, accordingTo)) {
            return false;
        }

        if (!DatabaseObjectComparatorFactory.getInstance().isSameObject(thisColumn.getRelation(), otherColumn.getRelation(), chain.getSchemaComparisons(), accordingTo)) {
            return false;
        }

        if (BooleanUtil.isTrue(thisColumn.getComputed()) != BooleanUtil.isTrue(otherColumn.getComputed())) {
            return false;
        }

        return BooleanUtil.isTrue(thisColumn.getDescending()) == BooleanUtil.isTrue(otherColumn.getDescending());
    }


    @Override
    public ObjectDifferences findDifferences(DatabaseObject databaseObject1, DatabaseObject databaseObject2, Database accordingTo, CompareControl compareControl, DatabaseObjectComparatorChain chain, Set<String> exclude) {
        exclude.add("name");
        exclude.add("type");
        exclude.add("autoIncrementInformation");

        if (!GlobalConfiguration.DIFF_COLUMN_ORDER.getCurrentValue()) {
            exclude.add("order");
        }

        ObjectDifferences differences = chain.findDifferences(databaseObject1, databaseObject2, accordingTo, compareControl, exclude);

        differences.compare("name", databaseObject1, databaseObject2, new ObjectDifferences.DatabaseObjectNameCompareFunction(Column.class, accordingTo));
        differences.compare("type", databaseObject1, databaseObject2, new ObjectDifferences.DatabaseObjectNameCompareFunction(Column.class, accordingTo));

        boolean autoIncrement1 = ((Column) databaseObject1).isAutoIncrement();
        boolean autoIncrement2 = ((Column) databaseObject2).isAutoIncrement();

        if (autoIncrement1 != autoIncrement2 && !compareControl.isSuppressedField(Column.class, "autoIncrementInformation")) { //only compare if autoIncrement or not since there are sometimes expected differences in start/increment/etc. value.
            differences.addDifference("autoIncrement", autoIncrement1, autoIncrement2);
        }
        if (accordingTo instanceof PostgresDatabase && autoIncrement1 && autoIncrement2) {
            String type1 = ((Column) databaseObject1).getType().getTypeName();
            String type2 = ((Column) databaseObject2).getType().getTypeName();
            boolean typesEquivalent = isPostgresAutoIncrementEquivalentType(type1, type2) || isPostgresAutoIncrementEquivalentType(type2, type1);
            if (typesEquivalent) {
                differences.removeDifference("type");
            }
        }

        return differences;
    }

    /**
     * Determine if the two types are essentially equivalent.
     * @param type1 first type to compare
     * @param type2 second type to compare
     * @return true if the types are essentially equivalent (bigserial and int8/bigint would be considered equivalent),
     * false otherwise
     */
    private boolean isPostgresAutoIncrementEquivalentType(String type1, String type2) {
        if (type1.equalsIgnoreCase(type2)) {
            return true;
        } else if (type1.equalsIgnoreCase("bigserial")) {
            return type2.equalsIgnoreCase("bigserial") || type2.equalsIgnoreCase("int8");
        } else if (type1.equalsIgnoreCase("serial")) {
            return type2.equalsIgnoreCase("serial") || type2.equalsIgnoreCase("int4");
        } else if (type1.equalsIgnoreCase("smallserial")) {
            return type2.equalsIgnoreCase("smallserial") || type2.equalsIgnoreCase("int2");
        }
        return false;
    }
}
