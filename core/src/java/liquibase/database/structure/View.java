package liquibase.database.structure;

import liquibase.database.Database;

import java.util.ArrayList;
import java.util.List;

public class View implements DatabaseObject, Comparable<View> {
    private Database database;
    private String name;
    private List<Column> columns = new ArrayList<Column>();
    private String definition;


    public View(String name) {
        this.name = name;
    }


    public View() {
    }


    public Database getDatabase() {
        return database;
    }

    public void setDatabase(Database database) {
        this.database = database;
    }

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


    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
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
