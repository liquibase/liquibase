package liquibase.diff.compare.core;

import liquibase.database.Database;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.compare.CompareControl;
import liquibase.structure.DatabaseObject;
import liquibase.diff.compare.DatabaseObjectComparator;
import liquibase.diff.compare.DatabaseObjectComparatorChain;
import liquibase.structure.core.Column;
import liquibase.structure.core.OldDataType;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public final class DefaultDatabaseObjectComparator implements DatabaseObjectComparator {
    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        return PRIORITY_DEFAULT;
    }

    @Override
    public String[] hash(DatabaseObject databaseObject, Database accordingTo, DatabaseObjectComparatorChain chain) {
        String name = databaseObject.getSimpleName();
        if (name == null) {
            name = "null";
        }
        return new String[] {name.toLowerCase()};
    }

    @Override
    public boolean isSameObject(DatabaseObject databaseObject1, DatabaseObject databaseObject2, Database accordingTo, DatabaseObjectComparatorChain chain) {
        if (databaseObject1.getClass().isAssignableFrom(databaseObject2.getClass()) || databaseObject2.getClass().isAssignableFrom(databaseObject1.getClass())) {
            return nameMatches(databaseObject1, databaseObject2, accordingTo);
        }
        return false;

    }

    @Override
    public ObjectDifferences findDifferences(DatabaseObject databaseObject1, DatabaseObject databaseObject2, Database accordingTo, CompareControl compareControl, DatabaseObjectComparatorChain chain, Set<String> exclude) {

        Set<String> attributes = new HashSet<String>();
        attributes.addAll(databaseObject1.getAttributeNames());
        attributes.addAll(databaseObject2.getAttributeNames());

        ObjectDifferences differences = new ObjectDifferences(compareControl);

        exclude.add("schema");
        exclude.add("catalog");

        for (String attribute : attributes) {
            if (exclude.contains(attribute)) {
                continue;
            }
            Object attribute1 = databaseObject1.get(attribute, Object.class);
            Object attribute2 = databaseObject2.get(attribute, Object.class);

            attribute1 = undoCollection(attribute1, attribute2);
            attribute2 = undoCollection(attribute2, attribute1);

            ObjectDifferences.CompareFunction compareFunction;
            if (attribute1 instanceof DatabaseObject || attribute2 instanceof DatabaseObject) {
                Class<? extends DatabaseObject> type;
                if (attribute1 != null) {
                    type = (Class<? extends DatabaseObject>) attribute1.getClass();
                } else {
                    type = (Class<? extends DatabaseObject>) attribute2.getClass();
                }
                compareFunction = new ObjectDifferences.DatabaseObjectNameCompareFunction(type, accordingTo);
            } else if (attribute1 instanceof OldDataType || attribute2 instanceof OldDataType) {
                compareFunction = new ObjectDifferences.ToStringCompareFunction(false);
            } else if (attribute1 instanceof Column.AutoIncrementInformation || attribute2 instanceof Column.AutoIncrementInformation) {
                compareFunction = new ObjectDifferences.ToStringCompareFunction(false);
            } else if (attribute1 instanceof Collection || attribute2 instanceof Collection) {
                compareFunction = new ObjectDifferences.OrderedCollectionCompareFunction(new ObjectDifferences.StandardCompareFunction(accordingTo));
            } else {
                compareFunction = new ObjectDifferences.StandardCompareFunction(accordingTo);
            }
            differences.compare(attribute, databaseObject1, databaseObject2, compareFunction);

        }

        return differences;
    }

    /**
     * Sometimes an attribute in one object is a single-entity collection and on the other it is just the object.
     * Check the passed potentialCollection and if it is a single-entry collection of the same type as the otherObject, return just the collection element.
     * Otherwise, return the original collection.
     */
    protected Object undoCollection(Object potentialCollection, Object otherObject) {
        if (potentialCollection != null && otherObject != null && potentialCollection instanceof Collection && !(otherObject instanceof Collection)) {
            if (((Collection) potentialCollection).size() == 1 && ((Collection) potentialCollection).iterator().next().getClass().equals(otherObject.getClass())) {
                potentialCollection = ((Collection) potentialCollection).iterator().next();
            }
        }
        return potentialCollection;
    }

    //Static so it can be used in other comparators if needed
    public static boolean nameMatches(DatabaseObject databaseObject1, DatabaseObject databaseObject2, Database accordingTo) {
        String object1Name = accordingTo.correctObjectName(databaseObject1.getSimpleName(), databaseObject1.getClass());
        String object2Name = accordingTo.correctObjectName(databaseObject2.getSimpleName(), databaseObject2.getClass());

        if (object1Name == null && object2Name == null) {
            return true;
        }
        if (object1Name == null || object2Name == null) {
            return false;
        }
        if (accordingTo.isCaseSensitive(databaseObject1.getClass())) {
            return object1Name.equals(object2Name);
        } else {
            return object1Name.equalsIgnoreCase(object2Name);
        }
    }

}
