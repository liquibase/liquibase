package liquibase.database.structure;

import liquibase.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class Index implements DatabaseObject, Comparable<Index> {
    private String name;
    private Table table;
    private List<String> columns = new ArrayList<String>();
    private String filterCondition;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public List<String> getColumns() {
        return columns;
    }

    public String getColumnNames() {
        return StringUtils.join(columns, ", ");
    }

    public String getFilterCondition() {
        return filterCondition;
    }

    public void setFilterCondition(String filterCondition) {
        this.filterCondition = filterCondition;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Index index = (Index) o;
        boolean equals = true;
        for (String column : index.getColumns()) {
            if (!columns.contains(column)) {
                equals = false;
            }
        }

        return equals && table.getName().equalsIgnoreCase(index.table.getName());

    }

    public int hashCode() {
        int result;
        result = table.getName().toUpperCase().hashCode();
        result = 31 * result + columns.hashCode();
        return result;
    }

    public int compareTo(Index o) {
        int returnValue = this.table.getName().compareTo(o.table.getName());

        if (returnValue == 0) {
            returnValue = this.getName().compareTo(o.getName());
        }

        //We should not have two indexes that have the same name and tablename
        /*if (returnValue == 0) {
        	returnValue = this.getColumnName().compareTo(o.getColumnName());
        }*/


        return returnValue;
    }

    public String toString() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(getName()).append(" on ").append(table.getName()).append("(");
        for (String column : columns) {
            stringBuffer.append(column).append(", ");
        }
        stringBuffer.delete(stringBuffer.length() - 2, stringBuffer.length());
        stringBuffer.append(")");
        return stringBuffer.toString();
    }

}
