package liquibase.diff;

import liquibase.database.Database;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.compare.DatabaseObjectComparatorFactory;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.DataType;

import java.math.BigDecimal;
import java.util.*;

public class ObjectDifferences {

    private CompareControl compareControl;
    private HashMap<String, Difference> differences = new HashMap<>();

    public ObjectDifferences(CompareControl compareControl) {
        this.compareControl = compareControl;
    }

    public Set<Difference> getDifferences() {
        return Collections.unmodifiableSet(new TreeSet<>(differences.values()));
    }

    public Difference getDifference(String field) {
        return differences.get(field);
    }

    public boolean isDifferent(String field) {
        return differences.containsKey(field);
    }

    public ObjectDifferences addDifference(String changedField, Object referenceValue, Object compareToValue) {
        this.differences.put(changedField, new Difference(changedField, referenceValue, compareToValue));

        return this;
    }

    public ObjectDifferences addDifference(String message, String changedField, Object referenceValue, Object compareToValue) {
        this.differences.put(changedField, new Difference(message, changedField, referenceValue, compareToValue));

        return this;
    }

    public boolean hasDifferences() {
        return !differences.isEmpty();
    }

    public void compare(String attribute, DatabaseObject referenceObject, DatabaseObject compareToObject, CompareFunction compareFunction) {
        compare(null, attribute, referenceObject, compareToObject, compareFunction);
    }
    public void compare(String message, String attribute, DatabaseObject referenceObject, DatabaseObject compareToObject, CompareFunction compareFunction) {
        if (compareControl.isSuppressedField(referenceObject.getClass(), attribute)) {
            return;
        }

        Object referenceValue = referenceObject.getAttribute(attribute, Object.class);
        Object compareValue = compareToObject.getAttribute(attribute, Object.class);

        referenceValue = undoCollection(referenceValue, compareValue);
        compareValue = undoCollection(compareValue, referenceValue);

        boolean different;
        if ((referenceValue == null) && (compareValue == null)) {
            different = false;
        } else if (((referenceValue == null) && (compareValue != null)) || ((referenceValue != null) && (compareValue
            == null))) {
            different = true;
        } else {
            different = !compareFunction.areEqual(referenceValue, compareValue);
        }

        if (different) {
            addDifference(message, attribute, referenceValue, compareValue);
        }

    }

    /**
     * Sometimes an attribute in one object is a single-entity collection and on the other it is just the object.
     * Check the passed potentialCollection and if it is a single-entry collection of the same type as the otherObject, return just the collection element.
     * Otherwise, return the original collection.
     */
    protected Object undoCollection(Object potentialCollection, Object otherObject) {
        if ((potentialCollection != null) && (otherObject != null) && (potentialCollection instanceof Collection) &&
            !(otherObject instanceof Collection)) {
            if ((((Collection) potentialCollection).size() == 1) && ((Collection) potentialCollection).iterator()
                .next().getClass().equals(otherObject.getClass())) {
                potentialCollection = ((Collection) potentialCollection).iterator().next();
            }
        }
        return potentialCollection;
    }


    public boolean removeDifference(String attribute) {
        return differences.remove(attribute) != null;
    }

    public CompareControl.SchemaComparison[] getSchemaComparisons() {
        return compareControl.getSchemaComparisons();
    }

    public interface CompareFunction {
        boolean areEqual(Object referenceValue, Object compareToValue);
    }

    public static class StandardCompareFunction implements CompareFunction {

        private final CompareControl.SchemaComparison[] schemaComparisons;
        private Database accordingTo;

        public StandardCompareFunction(CompareControl.SchemaComparison[] schemaComparisons, Database accordingTo) {
            this.schemaComparisons = schemaComparisons;
            this.accordingTo = accordingTo;
        }

        @Override
        public boolean areEqual(Object referenceValue, Object compareToValue) {
            if ((referenceValue == null) && (compareToValue == null)) {
                return true;
            }
            if ((referenceValue == null) || (compareToValue == null)) {
                return false;
            }

            if ((referenceValue instanceof DatabaseObject) && (compareToValue instanceof DatabaseObject)) {
                return DatabaseObjectComparatorFactory.getInstance().isSameObject((DatabaseObject) referenceValue, (DatabaseObject) compareToValue, schemaComparisons, accordingTo);
            } else {
                if ((referenceValue instanceof Number) && (compareToValue instanceof Number)
                        && !referenceValue.getClass().equals(compareToValue.getClass())) { //standardize on a common number type
                    referenceValue = new BigDecimal(referenceValue.toString());
                    compareToValue = new BigDecimal(compareToValue.toString());
                }
                if ((referenceValue instanceof Number) && (referenceValue instanceof Comparable)) {
                    return (compareToValue instanceof Number) && (((Comparable) referenceValue).compareTo
                        (compareToValue) == 0);
                } else {
                    return referenceValue.equals(compareToValue);
                }
            }
        }
    }

    public static class ToStringCompareFunction implements CompareFunction {

        private boolean caseSensitive;

        public ToStringCompareFunction(boolean caseSensitive) {
            this.caseSensitive = caseSensitive;
        }

        @Override
        public boolean areEqual(Object referenceValue, Object compareToValue) {
            if ((referenceValue == null) && (compareToValue == null)) {
                return true;
            }
            if ((referenceValue == null) || (compareToValue == null)) {
                return false;
            }

            if (caseSensitive) {
                return referenceValue.toString().equals(compareToValue.toString());
            } else {
                return referenceValue.toString().equalsIgnoreCase(compareToValue.toString());
            }

        }
    }

    public static class DatabaseObjectNameCompareFunction implements CompareFunction {

        private final Database accordingTo;
        private Class<? extends DatabaseObject> type;

        public DatabaseObjectNameCompareFunction(Class<? extends DatabaseObject> type, Database accordingTo) {
            this.type = type;
            this.accordingTo = accordingTo;

        }

        @Override
        public boolean areEqual(Object referenceValue, Object compareToValue) {
            if (referenceValue instanceof Collection) {
                if (!(compareToValue instanceof Collection)) {
                    return false;
                }
                if (((Collection) referenceValue).size() != ((Collection) compareToValue).size()) {
                    return false;
                } else {
                    Iterator referenceIterator = ((Collection) referenceValue).iterator();
                    Iterator compareToIterator = ((Collection) compareToValue).iterator();

                    while (referenceIterator.hasNext()) {
                        if (!areEqual(referenceIterator.next(), compareToIterator.next())) {
                            return false;
                        }
                    }
                    return true;
                }
            }

            if ((referenceValue == null) && (compareToValue == null)) {
                return true;
            }
            if ((referenceValue == null) || (compareToValue == null)) {
                return false;
            }


            String object1Name;
            if (referenceValue instanceof DatabaseObject) {
                object1Name = accordingTo.correctObjectName(((DatabaseObject) referenceValue).getAttribute("name", String.class), type);
            } else {
                object1Name = referenceValue.toString();
            }

            String object2Name;
            if (compareToValue instanceof DatabaseObject) {
                object2Name = accordingTo.correctObjectName(((DatabaseObject) compareToValue).getAttribute("name", String.class), type);
            } else {
                object2Name = compareToValue.toString();
            }

            if ((object1Name == null) && (object2Name == null)) {
                return true;
            }
            if ((object1Name == null) || (object2Name == null)) {
                return false;
            }
            if (accordingTo.isCaseSensitive()) {
                return object1Name.equals(object2Name);
            } else {
                return object1Name.equalsIgnoreCase(object2Name);
            }

        }
    }

    public static class DataTypeCompareFunction implements CompareFunction {

        private final Database accordingTo;

        public DataTypeCompareFunction(Database accordingTo) {
            this.accordingTo = accordingTo;

        }

        @Override
        public boolean areEqual(Object referenceValue, Object compareToValue) {
            if ((referenceValue == null) && (compareToValue == null)) {
                return true;
            }
            if ((referenceValue == null) || (compareToValue == null)) {
                return false;
            }

            DataType referenceType = (DataType) referenceValue;
            DataType compareToType = (DataType) compareToValue;

            if (!referenceType.getTypeName().equalsIgnoreCase(compareToType.getTypeName())) {
                return false;
            }

            if (compareToType.toString().contains("(") && referenceType.toString().contains("(")) {
                return compareToType.toString().equalsIgnoreCase(referenceType.toString());
            } else {
                return true;
            }



        }
    }

    public static class OrderedCollectionCompareFunction implements CompareFunction {

        private StandardCompareFunction compareFunction;

        public OrderedCollectionCompareFunction(StandardCompareFunction compareFunction) {
            this.compareFunction = compareFunction;
        }

        @Override
        public boolean areEqual(Object referenceValue, Object compareToValue) {
            if ((referenceValue == null) && (compareToValue == null)) {
                return true;
            }
            if ((referenceValue == null) || (compareToValue == null)) {
                return false;
            }

            if (!(referenceValue instanceof Collection) || (!(compareToValue instanceof Collection))) {
                return false;
            }

            if (((Collection) referenceValue).size() != ((Collection) compareToValue).size()) {
                return false;
            }

            Iterator referenceIterator = ((Collection) referenceValue).iterator();
            Iterator compareIterator = ((Collection) compareToValue).iterator();

            if (((Collection) referenceValue).size() != ((Collection) compareToValue).size()) {
                return false;
            }

            while (referenceIterator.hasNext()) {
                Object referenceObj = referenceIterator.next();
                Object compareObj = compareIterator.next();

                if (!compareFunction.areEqual(referenceObj, compareObj)) {
                    return false;
                }
            }

            return true;
        }
    }


    public static class UnOrderedCollectionCompareFunction implements CompareFunction {

        private StandardCompareFunction compareFunction;

        public UnOrderedCollectionCompareFunction(StandardCompareFunction compareFunction) {
            this.compareFunction = compareFunction;
        }

        @Override
        public boolean areEqual(Object referenceValue, Object compareToValue) {
            if ((referenceValue == null) && (compareToValue == null)) {
                return true;
            }
            if ((referenceValue == null) || (compareToValue == null)) {
                return false;
            }

            if (!(referenceValue instanceof Collection) || (!(compareToValue instanceof Collection))) {
                return false;
            }

            if (((Collection) referenceValue).size() != ((Collection) compareToValue).size()) {
                return false;
            }


            if (((Collection) referenceValue).size() != ((Collection) compareToValue).size()) {
                return false;
            }

            for (Object referenceObj : ((Collection) referenceValue)) {
                Object foundMatch = null;
                List<Object> unmatchedCompareToValues = new ArrayList<>(((Collection<?>) compareToValue));
                for (Object compareObj : unmatchedCompareToValues) {
                    if (compareFunction.areEqual(referenceObj, compareObj)) {
                        foundMatch = compareObj;
                        break;
                    }
                }
                if (foundMatch == null) {
                    return false;
                } else {
                    unmatchedCompareToValues.remove(foundMatch);
                }
            }

            return true;
        }
    }

}
