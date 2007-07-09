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

        Index that = (Index) o;

        return !(columnName != null ? !columnName.equals(that.columnName) : that.columnName != null) && !(name != null ? !name.equals(that.name) : that.name != null) && !(tableName != null ? !tableName.equals(that.tableName) : that.tableName != null);

    }

    public int hashCode() {
        int result;
        result = (name != null ? name.hashCode() : 0);
        result = 31 * result + (tableName != null ? tableName.hashCode() : 0);
        result = 31 * result + (columnName != null ? columnName.hashCode() : 0);
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
