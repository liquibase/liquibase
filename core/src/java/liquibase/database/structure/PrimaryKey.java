package liquibase.database.structure;

import liquibase.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class PrimaryKey implements DatabaseObject, Comparable<PrimaryKey> {
    private String name;
    private List<String> columnNames = new ArrayList<String>();
    private Table table;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColumnNames() {
        return StringUtils.join(columnNames, ", ");
    }

    public void addColumnName(int position, String columnName) {
        if (position >= columnNames.size()) {
            for (int i = columnNames.size()-1; i < position; i++) {
                this.columnNames.add(null);
            }
        }
        this.columnNames.set(position, columnName);
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }


    public int compareTo(PrimaryKey o) {
        int returnValue = this.getTable().getName().compareTo(o.getTable().getName());
        if (returnValue == 0) {
            returnValue = this.getColumnNames().compareTo(o.getColumnNames());
        }
//        if (returnValue == 0) {
//            returnValue = this.getName().compareTo(o.getName());
//        }

        return returnValue;
    }


    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PrimaryKey that = (PrimaryKey) o;

        return !(getColumnNames() != null ? !getColumnNames().equalsIgnoreCase(that.getColumnNames()) : that.getColumnNames() != null) && !(getTable().getName() != null ? !getTable().getName().equalsIgnoreCase(that.getTable().getName()) : that.getTable().getName() != null);

    }

    public int hashCode() {
        int result;
        result = (getColumnNames() != null ? getColumnNames().toUpperCase().hashCode() : 0);
        result = 31 * result + (table.getName() != null ? table.getName().toUpperCase().hashCode() : 0);
        return result;
    }

    public String toString() {
        return getName() + " on " + getTable().getName() + "(" + getColumnNames() + ")";
    }

    public List<String> getColumnNamesAsList() {
        return columnNames;
    }
}
