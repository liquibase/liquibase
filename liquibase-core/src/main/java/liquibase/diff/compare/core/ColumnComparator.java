package liquibase.diff.compare.core;

import liquibase.configuration.GlobalConfiguration;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.database.Database;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.compare.DatabaseObjectComparator;
import liquibase.diff.compare.DatabaseObjectComparatorChain;
import liquibase.diff.compare.DatabaseObjectComparatorFactory;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;
import liquibase.util.BooleanUtils;

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
        if (BooleanUtils.isTrue(column.getComputed())) {
            hash += ":computed";
        }
        if (BooleanUtils.isTrue(column.getDescending())) {
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

        //short circut chain.isSameObject for performance reasons. There can be a lot of columns in a database
        if (!DefaultDatabaseObjectComparator.nameMatches(thisColumn, otherColumn, accordingTo)) {
            return false;
        }

        if (!DatabaseObjectComparatorFactory.getInstance().isSameObject(thisColumn.getRelation(), otherColumn.getRelation(), chain.getSchemaComparisons(), accordingTo)) {
            return false;
        }

        if (BooleanUtils.isTrue(thisColumn.getComputed()) != BooleanUtils.isTrue(otherColumn.getComputed())) {
            return false;
        }

        if (BooleanUtils.isTrue(thisColumn.getDescending()) != BooleanUtils.isTrue(otherColumn.getDescending())) {
            return false;
        }

        return true;
    }


    @Override
    public ObjectDifferences findDifferences(DatabaseObject databaseObject1, DatabaseObject databaseObject2, Database accordingTo, CompareControl compareControl, DatabaseObjectComparatorChain chain, Set<String> exclude) {
        exclude.add("name");
        exclude.add("type");
        exclude.add("autoIncrementInformation");

        if (!LiquibaseConfiguration.getInstance().getConfiguration(GlobalConfiguration.class).getDiffColumnOrder()) {
            exclude.add("order");
        }

        ObjectDifferences differences = chain.findDifferences(databaseObject1, databaseObject2, accordingTo, compareControl, exclude);

        differences.compare("name", databaseObject1, databaseObject2, new ObjectDifferences.DatabaseObjectNameCompareFunction(Column.class, accordingTo));
        differences.compare("type", databaseObject1, databaseObject2, new ObjectDifferences.DatabaseObjectNameCompareFunction(Column.class, accordingTo));

        boolean autoIncrement1 = ((Column) databaseObject1).isAutoIncrement();
        boolean autoIncrement2 = ((Column) databaseObject2).isAutoIncrement();

        if (autoIncrement1 != autoIncrement2 && !compareControl.isSuppressedField(Column.class, "autoIncrementInformation")) { //only compare if autoIncrement or not since there are sometimes expected differences in start/increment/etc value.
            differences.addDifference("autoIncrement", autoIncrement1, autoIncrement2);
        }

        return differences;
    }
}
