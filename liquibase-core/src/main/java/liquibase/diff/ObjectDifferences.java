package liquibase.diff;

import liquibase.database.Database;
import liquibase.diff.compare.CompareControl;
import liquibase.structure.DatabaseObject;

import java.util.*;

public class ObjectDifferences {

    private CompareControl compareControl;
    private HashMap<String, Difference> differences = new HashMap<String, Difference>();

    public ObjectDifferences(CompareControl compareControl) {
        this.compareControl = compareControl;
    }

    public Set<Difference> getDifferences() {
        return Collections.unmodifiableSet(new HashSet<Difference>(differences.values()));
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
        return differences.size() > 0;
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

        boolean different;
        if (referenceValue == null && compareValue == null) {
            different = false;
        } else if ((referenceValue == null && compareValue != null) || (referenceValue != null && compareValue == null)) {
            different = true;
        } else {
            different = !compareFunction.areEqual(referenceValue, compareValue);
        }

        if (different) {
            addDifference(message, attribute, referenceValue, compareValue);
        }

    }

    public boolean removeDifference(String attribute) {
        return differences.remove(attribute) != null;
    }

    public interface CompareFunction {
        public boolean areEqual(Object referenceValue, Object compareToValue);
    }

    public static class StandardCompareFunction implements CompareFunction {

        public boolean areEqual(Object referenceValue, Object compareToValue) {
            if (referenceValue == null && compareToValue == null) {
                return true;
            }
            if (referenceValue == null || compareToValue == null) {
                return false;
            }

            return referenceValue.equals(compareToValue);

        }
    }

    public static class ToStringCompareFunction implements CompareFunction {

        public boolean areEqual(Object referenceValue, Object compareToValue) {
            if (referenceValue == null && compareToValue == null) {
                return true;
            }
            if (referenceValue == null || compareToValue == null) {
                return false;
            }

            return referenceValue.toString().equals(compareToValue.toString());

        }
    }

    public static class DatabaseObjectNameCompareFunction implements CompareFunction {

        private final Database accordingTo;
        private Class<? extends DatabaseObject> type;

        public DatabaseObjectNameCompareFunction(Class<? extends DatabaseObject> type, Database accordingTo) {
            this.type = type;
            this.accordingTo = accordingTo;

        }

        public boolean areEqual(Object referenceValue, Object compareToValue) {
            if (referenceValue instanceof Collection) {
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

            if (referenceValue == null && compareToValue == null) {
                return true;
            }
            if (referenceValue == null || compareToValue == null) {
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
                object2Name = referenceValue.toString();
            }

            if (object1Name == null && object2Name == null) {
                return true;
            }
            if (object1Name == null || object2Name == null) {
                return false;
            }
            if (accordingTo.isCaseSensitive()) {
                return object1Name.equals(object2Name);
            } else {
                return object1Name.equalsIgnoreCase(object2Name);
            }

        }
    }

    public static class OrderedCollectionCompareFunction implements CompareFunction {

        private StandardCompareFunction compareFunction;

        public OrderedCollectionCompareFunction(StandardCompareFunction compareFunction) {
            this.compareFunction = compareFunction;
        }

        public boolean areEqual(Object referenceValue, Object compareToValue) {
            if (referenceValue == null && compareToValue == null) {
                return true;
            }
            if (referenceValue == null || compareToValue == null) {
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

            while (referenceIterator.hasNext()) {
                Object referenceObj = referenceIterator.next();
                Object compareObj = compareIterator.next();

                if (referenceObj instanceof DatabaseObject) {
                    if (!compareFunction.areEqual(referenceObj, compareObj)) {
                        return false;
                    }
                }
            }

            for (Object obj : ((Collection) referenceValue)) {
                if (!((Collection) compareToValue).contains(obj)) {
                    return false;
                }
            }

            return referenceValue.equals(compareToValue);

        }
    }


}
