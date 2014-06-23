package liquibase.executor;

import java.util.*;

/**
 * Container for query results returned from database. If more complicated or memory-efficient logic is needed, it can be subclassed.
 */
public class QueryResult {

    private List<Row> resultSet;

    public QueryResult(Object singleValue) {
        if (singleValue == null) {
            this.resultSet = Collections.unmodifiableList(new ArrayList<Row>());
        } else if (singleValue instanceof List) {
            this.resultSet = Collections.unmodifiableList((List) singleValue);
        } else {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("value", singleValue);

            this.resultSet = Collections.unmodifiableList(Arrays.asList(new Row(map)));
        }
    }

    public QueryResult(List resultSet) {
        if (resultSet == null || resultSet.size() == 0) {
            this.resultSet = Collections.unmodifiableList(new ArrayList<Row>());
        } else {
            if (resultSet.get(0) instanceof Map) {
                List<Row> convertedResultSet = new ArrayList<Row>();
                for (Map map : (List<Map>) resultSet) {
                    convertedResultSet.add(new Row(map));
                }
                resultSet = convertedResultSet;
            }
            this.resultSet = Collections.unmodifiableList((List<Row>) resultSet);
        }
    }

    /**
     * Return a single object of the given type. Throws exception if there are more than one row in this QueryResult or
     * if there is more than one value in the row. Returns null if this QueryResult is empty or the single row is empty.
     * Will convert the requiredType if needed via {@link liquibase.util.ObjectUtil#convert(Object, Class)}
     */
    public <T> T toObject(Class<T> requiredType) throws IllegalArgumentException {
        Row singleRow = getSingleRow();
        if (singleRow == null) {
            return null;
        }
        return singleRow.getSingleValue(requiredType);
    }

    /**
     * Returns a single object of the given type. Returns the passed defaultValue if the value is null
     */
    public <T> T toObject(T defaultValue) throws IllegalArgumentException {
        Row singleRow = getSingleRow();
        if (singleRow == null) {
            return defaultValue;
        }
        return singleRow.getSingleValue(defaultValue);
    }

    /**
     * Return a list of objects of the given type. Throws exception if there is more than one value in the row.
     * Returns an empty if this QueryResult was originally passed a null collection.
     * Will convert the requiredType if needed via {@link liquibase.util.ObjectUtil#convert(Object, Class)}
     */
    public <T> List<T> toList(Class<T> elementType) throws IllegalArgumentException {
        List returnList = new ArrayList();
        for (Row row : resultSet) {
            returnList.add(row.getSingleValue(elementType));
        }
        return Collections.unmodifiableList(returnList);
    }

    /**
     * Return a list of map objects corresponding to this QueryResult.
     * Returns an empty collection if this QueryResult was originally passed a null collection.
     */
    public List<Row> toList() throws IllegalArgumentException {
        return resultSet;
    }

    /**
     * Extract a single row from this QueryResult. Returns null if the original collection was null or empty.
     * Throws exception if there is more than one row.
     */
    protected Row getSingleRow() throws IllegalArgumentException {
        if (resultSet.size() == 0) {
            return null;
        }
        if (resultSet.size() > 1) {
            throw new IllegalArgumentException("Results contained "+resultSet.size()+" rows");
        }
        return resultSet.get(0);
    }

    public int size() {
        return resultSet.size();
    }

}
