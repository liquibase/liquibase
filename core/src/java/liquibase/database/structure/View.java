package liquibase.database.structure;

import java.util.ArrayList;
import java.util.List;

public class View implements DatabaseObject, Comparable<View> {
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

    public void addColumn(Column column) {
        columns.add(column);
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        View view = (View) o;

        return name.equalsIgnoreCase(view.name);

    }

    public int hashCode() {
        return name.toUpperCase().hashCode();
    }

    public int compareTo(View o) {
        return this.getName().compareTo(o.getName());
    }


    public String toString() {
        return getName();
    }
}
