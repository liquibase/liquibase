package liquibase.database.structure;

import java.util.ArrayList;
import java.util.List;

public class Table implements DatabaseObject, Comparable<Table> {
    private String name;
    private List<Column> columns = new ArrayList<Column>();


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Column> getColumns() {
        return columns;
    }


    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Table that = (Table) o;

        return name.equalsIgnoreCase(that.name);

    }

    public int hashCode() {
        return name.toUpperCase().hashCode();
    }


    public int compareTo(Table o) {
        return this.getName().compareTo(o.getName());
    }


    public String toString() {
        return getName();
    }

    /**
     * Returns the column object for the given columnName.  If the column does not exist in this table,
     * return null.
     */
    public Column getColumn(String columnName) {
        for (Column column : getColumns()) {
            if (column.getName().equalsIgnoreCase(columnName)) {
                return column;
            }
        }
        return null;
    }
}
