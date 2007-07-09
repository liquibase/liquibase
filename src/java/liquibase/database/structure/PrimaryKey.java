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

        return columnNames.equals(that.columnNames) && !(name != null ? !name.equals(that.name) : that.name != null) && tableName.equals(that.tableName);

    }

    public int hashCode() {
        int result;
        result = (name != null ? name.hashCode() : 0);
        result = 31 * result + columnNames.hashCode();
        result = 31 * result + tableName.hashCode();
        return result;
    }


    public int compareTo(PrimaryKey o) {
        return this.getName().compareTo(o.getName());
    }


    public String toString() {
        return getName()+" on "+getTableName()+"("+getColumnNames()+")";
    }
}
