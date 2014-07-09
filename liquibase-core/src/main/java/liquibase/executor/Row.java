package liquibase.executor;

import liquibase.util.ObjectUtil;
import liquibase.util.StringUtils;

import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;

public class Row {

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
     * Returns the value in the passed column, converted to the given type via {@link ObjectUtil#convert(Object, Class)}.
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
     * The column value will be converted to the type of the default value via {@link ObjectUtil#convert(Object, Class)} if necessary.
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
        return "["+StringUtils.join(data, ", ", new StringUtils.ToStringFormatter())+"]";
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
