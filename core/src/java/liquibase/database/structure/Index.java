package liquibase.database.structure;

public class Index implements Comparable<Index> {
    private String name;
    private String tableName;
    private String columnName;


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

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }


    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Index index = (Index) o;

        return columnName.equals(index.columnName) && tableName.equals(index.tableName);

    }

    public int hashCode() {
        int result;
        result = tableName.hashCode();
        result = 31 * result + columnName.hashCode();
        return result;
    }

    public int compareTo(Index o) {
        int returnValue = this.getTableName().compareTo(o.getTableName());

        if (returnValue == 0) {
            returnValue = this.getColumnName().compareTo(o.getColumnName());
        }

        if (returnValue == 0) {
            returnValue = this.getName().compareTo(o.getName());
        }

        return returnValue;
    }

    public String toString() {
        return getName()+" on "+getTableName()+"("+getColumnName()+")";
    }

}
