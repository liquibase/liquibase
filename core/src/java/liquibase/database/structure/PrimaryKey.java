package liquibase.database.structure;

import liquibase.util.StringUtils;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.List;
import java.util.ArrayList;

public class PrimaryKey implements Comparable<PrimaryKey> {
    private String name;
    private List<String> columnNames = new ArrayList<String>();
    private String tableName;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColumnNames() {
        return StringUtils.join(columnNames, ", ");
    }

    public void addColumnName(String columnName) {
        this.columnNames.add(columnName);
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }


    public int compareTo(PrimaryKey o) {
        int returnValue = this.getTableName().compareTo(o.getTableName());
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

        return !(getColumnNames() != null ? !getColumnNames().equals(that.getColumnNames()) : that.getColumnNames() != null) && !(tableName != null ? !tableName.equals(that.tableName) : that.tableName != null);

    }

    public int hashCode() {
        int result;
        result = (getColumnNames() != null ? getColumnNames().hashCode() : 0);
        result = 31 * result + (tableName != null ? tableName.hashCode() : 0);
        return result;
    }

    public String toString() {
        return getName()+" on "+getTableName()+"("+getColumnNames()+")";
    }
}
