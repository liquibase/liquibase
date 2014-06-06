package liquibase.executor;

import liquibase.util.ObjectUtil;

import java.util.*;

/**
 * Container for query results returned from database. If more complicated or memory-efficient logic is needed, it can be subclassed.
 */
public class QueryResult {

    private List<Map<String, Object>> resultSet;

    public QueryResult(Object singleValue) {
        if (singleValue == null) {
            this.resultSet = Collections.unmodifiableList(new ArrayList<Map<String, Object>>());
        } else {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("value", singleValue);

            this.resultSet = Collections.unmodifiableList(Arrays.asList(map));
        }
    }

    public QueryResult(List<Map<String, Object>> resultSet) {
        if (resultSet == null) {
            this.resultSet = Collections.unmodifiableList(new ArrayList<Map<String, Object>>());
        } else {
            this.resultSet = Collections.unmodifiableList(resultSet);
        }
    }

    /**
     * Return a single object of the given type. Throws exception if there are more than one row in this QueryResult or
     * if there is more than one value in the row. Returns null if this QueryResult is empty or the single row is empty.
     * Will convert the requiredType if needed via {@link liquibase.util.ObjectUtil#convert(Object, Class)}
     */
    public <T> T toObject(Class<T> requiredType) throws IllegalArgumentException {
        return getSingleValue(getSingleRow(), requiredType);
    }

    /**
     * Returns a single object of the given type. Returns the passed defaultValue if the value is null
     */
    public <T> T toObject(T defaultValue) throws IllegalArgumentException {
        if (defaultValue == null) {
            return null;
        }
        T value = (T) getSingleValue(getSingleRow(), defaultValue.getClass());
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    /**
     * Return a list of objects of the given type. Throws exception if there is more than one value in the row.
     * Returns an empty if this QueryResult was originally passed a null collection.
     * Will convert the requiredType if needed via {@link liquibase.util.ObjectUtil#convert(Object, Class)}
     */
    public <T> List<T> toList(Class<T> elementType) throws IllegalArgumentException {
        List returnList = new ArrayList();
        for (Map<String, Object> row : resultSet) {
            returnList.add(getSingleValue(row, elementType));
        }
        return Collections.unmodifiableList(returnList);
    }

    /**
     * Return a list of map objects corresponding to this QueryResult.
     * Returns an empty collection if this QueryResult was originally passed a null collection.
     */
    public List<Map<String, ?>> toList() throws IllegalArgumentException {
        return (List<Map<String, ?>>) (List) resultSet;
    }

    /**
     * Extract a single row from this QueryResult. Returns null if the original collection was null or empty.
     * Throws exception if there is more than one row.
     */
    protected Map<String, Object> getSingleRow() throws IllegalArgumentException {
        if (resultSet.size() == 0) {
            return null;
        }
        if (resultSet.size() > 1) {
            throw new IllegalArgumentException("Results contained "+resultSet.size()+" rows");
        }
        return resultSet.get(0);
    }

    /**
     * Extracts the single value of the given row as the passed type. Returns null if the row is null or empty.
     * Throws an exception if the row has more than one value.
     * Will convert the requiredType if needed via {@link liquibase.util.ObjectUtil#convert(Object, Class)}
     */
    protected  <T> T getSingleValue(Map <String, Object> row, Class<T> type) throws IllegalArgumentException {
        if (row == null || row.size() == 0) {
            return null;
        }
        if (row.size() > 1) {
            throw new IllegalArgumentException("Row contained "+row.size()+" values");
        }
        Object value = row.values().iterator().next();
        if (value == null) {
            return null;
        }
        return ObjectUtil.convert(value, type);
    }



}
