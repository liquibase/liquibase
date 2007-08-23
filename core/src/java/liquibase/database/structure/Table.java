package liquibase.database.structure;

import java.util.ArrayList;
import java.util.List;

public class Table implements Comparable<Table> {
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

        return name.equals(that.name);

    }

    public int hashCode() {
        return name.hashCode();
    }


    public int compareTo(Table o) {
        return this.getName().compareTo(o.getName());
    }


    public String toString() {
        return getName();
    }
}
