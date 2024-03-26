package liquibase.snapshot;

import java.util.Map;

public class CachedRow {
    private final Map row;

    public CachedRow(Map row) {
        this.row = row;
    }



    public Object get(String columnName) {
        return row.get(columnName);
    }

    public void set(String columnName, Object value) {
        row.put(columnName, value);
    }


    public boolean containsColumn(String columnName) {
        return row.containsKey(columnName);
    }

    public String getString(String columnName) {
        return (String) row.get(columnName);
    }

    public Integer getInt(String columnName) {
        Object o = row.get(columnName);
        if (o instanceof Number) {
            return ((Number) o).intValue();
        } else if (o instanceof String) {
            return Integer.valueOf((String) o);
        } else if (o instanceof byte[]) {
            //
            // Added this condition after finding that Clustrix (MariaDB/MySQL)
            // returns the size value as a byte[] that contains the ASCII values
            // of the numbers
            //
            return Integer.valueOf(new String((byte[]) o));
        }
        return (Integer) o;
    }

    public Short getShort(String columnName) {
        Object o = row.get(columnName);
        if (o instanceof Number) {
            return ((Number) o).shortValue();
        } else if (o instanceof String) {
            return Short.valueOf((String) o);
        }
        return (Short) o;
    }

    public Boolean getBoolean(String columnName) {
        Object o = row.get(columnName);
        if (o instanceof Number) {
            return ((Number) o).longValue() != 0;
        }
        if (o instanceof String) {
            String s = (String)o;
            // Firebird JDBC driver quirk:
            // Returns "T" instead of "true" (Boolean.valueOf tests case-insensitively for "true")
            if ("T".equalsIgnoreCase(s))
                s = "TRUE";
            return Boolean.valueOf(s);
        }
        return (Boolean) o;
    }

    /**
     * Converts a 'YES'/'NO' value to a boolean value.
     *
     * @param columnName the name of the column whose value should be converted
     * @return {@code true} if the column value is 'YES', {@code false} otherwise; or {@code null} if the column value is {@code null}
     */
    public Boolean yesNoToBoolean(String columnName) {
        Object o = row.get(columnName);
        if (o instanceof String && "YES".equalsIgnoreCase((String)o)) {
            return Boolean.TRUE;
        }
        return getBoolean(columnName);
    }
}
