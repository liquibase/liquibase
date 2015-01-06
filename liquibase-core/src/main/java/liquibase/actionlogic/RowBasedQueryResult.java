package liquibase.actionlogic;

import liquibase.util.ObjectUtil;
import liquibase.util.StringUtils;

import java.util.*;

/**
 * Implementation of QueryResult designed for databases and other systems that return row-based query results.
 * If more complex or memory-efficient logic is needed, it can be subclassed.
 */
public class RowBasedQueryResult extends QueryResult {
    private List<Row> resultSet;

    public RowBasedQueryResult(Object singleValue, String message) {
        super(message);
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

    public RowBasedQueryResult(Object singleValue) {
        this(singleValue, null);
    }

    public RowBasedQueryResult(List resultSet, String message) {
        super(message);
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

    public RowBasedQueryResult(List resultSet) {
        this(resultSet, null);
    }


    /**
     * Return a single object of the given type. Throws exception if there are more than one row in this QueryResult or
     * if there is more than one value in the row. Returns null if this QueryResult is empty or the single row is empty.
     * Will convert the requiredType if needed via {@link liquibase.util.ObjectUtil#convert(Object, Class)}
     */
    public <T> T asObject(Class<T> requiredType) throws IllegalArgumentException {
        Row singleRow = getSingleRow();
        if (singleRow == null) {
            return null;
        }
        return singleRow.getSingleValue(requiredType);
    }

    /**
     * Returns a single object of the given type. Returns the passed defaultValue if the value is null
     */
    public <T> T asObject(T defaultValue) throws IllegalArgumentException {
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
    public <T> List<T> asList(Class<T> elementType) throws IllegalArgumentException {
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

    public static class Row {

        private SortedMap<String, ?> data;

        public Row(Map<String, ?> data) {
            if (data == null) {
                this.data = new TreeMap<String, Object>();
            } else {
                this.data = new TreeMap<String, Object>(data);
            }
        }

        public int size() {
            return data.size();
        }

        public SortedSet<String> getColumns() {
            return (SortedSet) data.keySet();
        }

        public boolean hasColumn(String column) {
            return data.containsKey(column);
        }

        /**
         * Extracts the single value of the given row as the passed type. Returns null if the row is null or empty.
         * Throws an exception if the row has more than one value.
         * Will convert the requiredType if needed via {@link liquibase.util.ObjectUtil#convert(Object, Class)}
         */
        public  <T> T getSingleValue(Class<T> type) throws IllegalArgumentException {
            if (data.size() == 0) {
                return null;
            }
            if (data.size() > 1) {
                throw new IllegalArgumentException("Row contained "+data.size()+" values");
            }

            return get(data.firstKey(), type);
        }

        /**
         * Extracts the single value of the given row as the passed type. Returns null if the row is null or empty.
         * Throws an exception if the row has more than one value.
         * Will convert the requiredType if needed via {@link liquibase.util.ObjectUtil#convert(Object, Class)}
         */
        public  <T> T getSingleValue(T defaultValue) throws IllegalArgumentException {
            if (data.size() == 0) {
                return null;
            }
            if (data.size() > 1) {
                throw new IllegalArgumentException("Row contained "+data.size()+" values");
            }

            return get(data.firstKey(), defaultValue);
        }

        /**
         * Returns the value in the passed column, converted to the given type via {@link liquibase.util.ObjectUtil#convert(Object, Class)}.
         * Returns null if the row is null or empty.
         * Returns null if the column does not exist in the row.
         */
        public <T> T get(String column, Class<T> type) {
            if (!data.containsKey(column)) {
                return null;
            }
            return ObjectUtil.convert(data.get(column), type);
        }

        /**
         * Returns the value in the passed column, or the passed default value if the column is null.
         * The column value will be converted to the type of the default value via {@link liquibase.util.ObjectUtil#convert(Object, Class)} if necessary.
         * Returns the default value if the column does not exist in the row.
         */
        public <T> T get(String column, T defaultValue) {
            if (data.get(column) == null) {
                return defaultValue;
            }
            if (defaultValue == null) {
                return (T) data.get(column);
            }
            return (T) ObjectUtil.convert(data.get(column), defaultValue.getClass());

        }


        @Override
        public String toString() {
            return "["+ StringUtils.join(data, ", ", new StringUtils.ToStringFormatter())+"]";
        }

        @Override
        public int hashCode() {
            return toString().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Row && this.toString().equals(obj.toString());
        }
    }
}
