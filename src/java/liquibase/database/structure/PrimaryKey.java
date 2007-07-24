package liquibase.database.structure;

public class PrimaryKey implements Comparable<PrimaryKey> {
    private String name;
    private String columnNames;
    private String tableName;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColumnNames() {
        return columnNames;
    }

    public void setColumnNames(String columnNames) {
        this.columnNames = columnNames;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }


    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PrimaryKey that = (PrimaryKey) o;

        return columnNames.equals(that.columnNames) && tableName.equals(that.tableName);

    }


    public int compareTo(PrimaryKey o) {
        int returnValue = this.getTableName().compareTo(o.getTableName());
        if (returnValue == 0) {
            returnValue = this.getColumnNames().compareTo(o.getColumnNames());
        }
        if (returnValue == 0) {
            returnValue = this.getName().compareTo(o.getName());
        }

        return returnValue;
    }

    public int hashCode() {
        int result;
        result = columnNames.hashCode();
        result = 31 * result + tableName.hashCode();
        return result;
    }

    public String toString() {
        return getName()+" on "+getTableName()+"("+getColumnNames()+")";
    }
}
