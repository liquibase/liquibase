package liquibase.database.structure;

public class ForeignKey implements Comparable<ForeignKey> {
    private Table primaryKeyTable;
    private String primaryKeyColumn;

    private Table foreignKeyTable;
    private String foreignKeyColumn;

    private String name;

    private Boolean deferrable;
    private Boolean initiallyDeferred;


    public Table getPrimaryKeyTable() {
        return primaryKeyTable;
    }

    public void setPrimaryKeyTable(Table primaryKeyTable) {
        this.primaryKeyTable = primaryKeyTable;
    }

    public String getPrimaryKeyColumn() {
        return primaryKeyColumn;
    }

    public void setPrimaryKeyColumn(String primaryKeyColumn) {
        this.primaryKeyColumn = primaryKeyColumn;
    }

    public Table getForeignKeyTable() {
        return foreignKeyTable;
    }

    public void setForeignKeyTable(Table foreignKeyTable) {
        this.foreignKeyTable = foreignKeyTable;
    }

    public String getForeignKeyColumn() {
        return foreignKeyColumn;
    }

    public void setForeignKeyColumn(String foreignKeyColumn) {
        this.foreignKeyColumn = foreignKeyColumn;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String toString() {
        return getName();
    }


    public Boolean isDeferrable() {
        return deferrable;
    }

    public void setDeferrable(Boolean deferrable) {
        this.deferrable = deferrable;
    }


    public Boolean isInitiallyDeferred() {
        return initiallyDeferred;
    }

    public void setInitiallyDeferred(Boolean initiallyDeferred) {
        this.initiallyDeferred = initiallyDeferred;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ForeignKey that = (ForeignKey) o;

        return foreignKeyColumn.equals(that.foreignKeyColumn)
                && foreignKeyTable.equals(that.foreignKeyTable)
                && !(name != null ? !name.equals(that.name) : that.name != null)
                && primaryKeyColumn.equals(that.primaryKeyColumn)
                && primaryKeyTable.equals(that.primaryKeyTable);

    }

    public int hashCode() {
        int result;
        result = primaryKeyTable.hashCode();
        result = 31 * result + primaryKeyColumn.hashCode();
        result = 31 * result + foreignKeyTable.hashCode();
        result = 31 * result + foreignKeyColumn.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }


    public int compareTo(ForeignKey o) {
        int returnValue = this.getForeignKeyTable().compareTo(o.getPrimaryKeyTable());

        if (returnValue == 0) {
            returnValue = this.getForeignKeyColumn().compareTo(o.getForeignKeyColumn());
        }

        if (returnValue == 0) {
            returnValue = this.getPrimaryKeyTable().compareTo(o.getPrimaryKeyTable());
        }

        if (returnValue == 0) {
            returnValue = this.getPrimaryKeyColumn().compareTo(o.getPrimaryKeyColumn());
        }

        if (returnValue == 0) {
            returnValue = this.getName().compareTo(o.getName());
        }
        
        return returnValue;
    }
}
