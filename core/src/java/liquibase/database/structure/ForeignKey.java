package liquibase.database.structure;

public class ForeignKey implements DatabaseObject, Comparable<ForeignKey> {
    private Table primaryKeyTable;
    private String primaryKeyColumn;

    private Table foreignKeyTable;
    private String foreignKeyColumn;

    private String name;

    private boolean deferrable;
    private boolean initiallyDeferred;


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
        return getName()+" ("+getPrimaryKeyColumn()+" -> "+getForeignKeyColumn()+")";
    }


    public boolean isDeferrable() {
        return deferrable;
    }

    public void setDeferrable(boolean deferrable) {
        this.deferrable = deferrable;
    }


    public boolean isInitiallyDeferred() {
        return initiallyDeferred;
    }

    public void setInitiallyDeferred(boolean initiallyDeferred) {
        this.initiallyDeferred = initiallyDeferred;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ForeignKey that = (ForeignKey) o;

        return foreignKeyColumn.equalsIgnoreCase(that.foreignKeyColumn)
                && foreignKeyTable.equals(that.foreignKeyTable)
                && primaryKeyColumn.equalsIgnoreCase(that.primaryKeyColumn)
                && primaryKeyTable.equals(that.primaryKeyTable);

    }

    public int hashCode() {
        int result = 0;
        if (primaryKeyTable != null) {
            result = primaryKeyTable.hashCode();
        }

        if (primaryKeyColumn != null) {
            result = 31 * result + primaryKeyColumn.toUpperCase().hashCode();
        }

        if (foreignKeyTable != null) {
            result = 31 * result + foreignKeyTable.hashCode();
        }

        if (foreignKeyColumn != null) {
            result = 31 * result + foreignKeyColumn.toUpperCase().hashCode();
        }

        return result;
    }


    public int compareTo(ForeignKey o) {
        int returnValue = 0;
        if (this.getForeignKeyTable() != null && o.getForeignKeyTable() != null) {
            returnValue = this.getForeignKeyTable().compareTo(o.getForeignKeyTable());
        }

        if (returnValue == 0 && this.getForeignKeyColumn() != null && o.getForeignKeyColumn() != null) {
            returnValue = this.getForeignKeyColumn().compareTo(o.getForeignKeyColumn());
        }

        if (returnValue == 0 && this.getPrimaryKeyTable() != null && o.getPrimaryKeyTable() != null) {
            returnValue = this.getPrimaryKeyTable().compareTo(o.getPrimaryKeyTable());
        }

        if (returnValue == 0 && this.getPrimaryKeyColumn() != null && o.getPrimaryKeyColumn() != null) {
            returnValue = this.getPrimaryKeyColumn().compareTo(o.getPrimaryKeyColumn());
        }

        return returnValue;
    }
}
