package liquibase.database.structure;

import liquibase.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class Index implements DatabaseObject, Comparable<Index> {
    private String name;
    private String tableName;
    private List<String> columns = new ArrayList<String>();
    private String filterCondition;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
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

        return equals && tableName.equalsIgnoreCase(index.tableName);

    }

    public int hashCode() {
        int result;
        result = tableName.toUpperCase().hashCode();
        result = 31 * result + columns.hashCode();
        return result;
    }

    public int compareTo(Index o) {
        int returnValue = this.getTableName().compareTo(o.getTableName());

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
        stringBuffer.append(getName() + " on " + getTableName() + "(");
        for (String column : columns) {
            stringBuffer.append(column + ", ");
        }
        stringBuffer.delete(stringBuffer.length() - 2, stringBuffer.length());
        stringBuffer.append(")");
        return stringBuffer.toString();
    }

}
