package liquibase.database.struture;


import java.sql.Connection;

public class ForeignKey implements DatabaseStructure {
    protected Table primaryKeyTable;
    protected String primaryKeyColumnName;
    protected Table foreignKeyTable;
    protected String foreignKeyColumnName;
    protected String foreignKeyName;
    protected String primaryKeyName;

    public ForeignKey(Table primaryKeyTable, String primaryKeyColumnName, Table foreignKeyTable, String foreignKeyColumnName, String foreignKeyName, String primaryKeyName) {
        this.primaryKeyTable = primaryKeyTable;
        this.primaryKeyColumnName = primaryKeyColumnName;
        this.foreignKeyTable = foreignKeyTable;
        this.foreignKeyColumnName = foreignKeyColumnName;
        this.foreignKeyName = foreignKeyName;
        this.primaryKeyName = primaryKeyName;
    }

    public Table getPrimaryKeyTable() {
        return primaryKeyTable;
    }

    public String getPrimaryKeyColumnName() {
        return primaryKeyColumnName;
    }

    public Table getForeignKeyTable() {
        return foreignKeyTable;
    }

    public String getForeignKeyColumnName() {
        return foreignKeyColumnName;
    }

    public String getForeignKeyName() {
        return foreignKeyName;
    }

    public String getPrimaryKeyName() {
        return primaryKeyName;
    }

    public String toString() {
        return getForeignKeyTable().getName()+"."+getForeignKeyColumnName()+"->"+getPrimaryKeyTable().getName()+"."+getPrimaryKeyColumnName();
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final ForeignKey that = (ForeignKey) o;

        if (!foreignKeyColumnName.equals(that.foreignKeyColumnName)) return false;
        if (!primaryKeyColumnName.equals(that.primaryKeyColumnName)) return false;
        if (!primaryKeyTable.equals(that.primaryKeyTable)) return false;
        if (!foreignKeyTable.equals(that.foreignKeyTable)) return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = foreignKeyName.hashCode();
        result = 29 * result + primaryKeyName.hashCode();
        return result;
    }

    public int compareTo(Object o) {
        if (o instanceof ForeignKey) {
            return toString().compareTo(o.toString());
        } else {
            return getClass().getName().compareTo(o.getClass().getName());
        }
    }

    public Connection getConnection() {
        return getPrimaryKeyTable().getConnection();
    }
}
