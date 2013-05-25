package liquibase.diff;

import liquibase.database.Database;
import liquibase.structure.DatabaseObject;

import java.util.*;

public class ObjectDifferences {
    private HashMap<String, Difference> differences = new HashMap<String, Difference>();

    public Set<Difference> getDifferences() {
        return Collections.unmodifiableSet(new HashSet<Difference>(differences.values()));
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
        Object referenceValue = referenceObject.getAttribute(attribute, Object.class);
        Object compareValue = compareToObject.getAttribute(attribute, Object.class);

        boolean different;
        if (referenceValue == null && compareValue == null) {
            different = true;
        } else if (referenceValue == null || compareValue == null) {
            different = false;
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
            String object1Name = accordingTo.correctObjectName((String) referenceValue, type);
            String object2Name = accordingTo.correctObjectName((String) compareToValue, type);

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

}
