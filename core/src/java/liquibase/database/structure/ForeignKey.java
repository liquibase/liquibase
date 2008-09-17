package liquibase.database.structure;

import java.util.List;

public class ForeignKey implements DatabaseObject, Comparable<ForeignKey> {
    private Table primaryKeyTable;
    private String primaryKeyColumns;

    private Table foreignKeyTable;
    private String foreignKeyColumns;

    private String name;

    private boolean deferrable;
    private boolean initiallyDeferred;

    private Integer updateRule;
    private Integer deleteRule;


    public Table getPrimaryKeyTable() {
        return primaryKeyTable;
    }

    public void setPrimaryKeyTable(Table primaryKeyTable) {
        this.primaryKeyTable = primaryKeyTable;
    }

    public String getPrimaryKeyColumns() {
        return primaryKeyColumns;
    }

    public void addPrimaryKeyColumn(String primaryKeyColumn) {
        if ((this.primaryKeyColumns == null)
                || (this.primaryKeyColumns.length() == 0)) {
            this.primaryKeyColumns = primaryKeyColumn;
        } else {
            this.primaryKeyColumns = this.primaryKeyColumns + ", "
                    + primaryKeyColumn;
        }
    }

    public void setPrimaryKeyColumns(String primaryKeyColumns) {
        this.primaryKeyColumns = primaryKeyColumns;
    }

    public Table getForeignKeyTable() {
        return foreignKeyTable;
    }

    public void setForeignKeyTable(Table foreignKeyTable) {
        this.foreignKeyTable = foreignKeyTable;
    }

    public String getForeignKeyColumns() {
        return foreignKeyColumns;
    }

    public void addForeignKeyColumn(String foreignKeyColumn) {
        if ((this.foreignKeyColumns == null)
                || (this.foreignKeyColumns.length() == 0)) {
            this.foreignKeyColumns = foreignKeyColumn;
        } else {
            this.foreignKeyColumns = this.foreignKeyColumns + ", "
                    + foreignKeyColumn;
        }
    }

    public void setForeignKeyColumns(String foreignKeyColumns) {
        this.foreignKeyColumns = foreignKeyColumns;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String toString() {
        return getName() + "(" + getForeignKeyTable() + "." + getForeignKeyColumns() + " ->" + getPrimaryKeyTable() + "." + getPrimaryKeyColumns() + ")";
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

    public void setUpdateRule(Integer rule) {
        this.updateRule = rule;
    }

    public Integer getUpdateRule() {
        return this.updateRule;
    }

    public void setDeleteRule(Integer rule) {
        this.deleteRule = rule;
    }

    public Integer getDeleteRule() {
        return this.deleteRule;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ForeignKey that = (ForeignKey) o;

        return getForeignKeyColumns().equalsIgnoreCase(that.getForeignKeyColumns())
                && foreignKeyTable.equals(that.foreignKeyTable)
                && getPrimaryKeyColumns().equalsIgnoreCase(that.getPrimaryKeyColumns())
                && primaryKeyTable.equals(that.primaryKeyTable);

    }

    public int hashCode() {
        int result = 0;
        if (primaryKeyTable != null) {
            result = primaryKeyTable.hashCode();
        }
        if (primaryKeyColumns != null) {
            result = 31 * result + primaryKeyColumns.toUpperCase().hashCode();
        }

        if (foreignKeyTable != null) {
            result = 31 * result + foreignKeyTable.hashCode();
        }

        if (foreignKeyColumns != null) {
            result = 31 * result + foreignKeyColumns.toUpperCase().hashCode();
        }
        if (this.updateRule != null)
            result = 31 * result + this.updateRule.hashCode();
        if (this.deleteRule != null)
            result = 31 * result + this.deleteRule.hashCode();


        return result;
    }


    public int compareTo(ForeignKey o) {
        int returnValue = 0;
        if (this.getForeignKeyTable() != null && o.getForeignKeyTable() != null) {
            returnValue = this.getForeignKeyTable().compareTo(o.getForeignKeyTable());
        }
        if (returnValue == 0 && this.getForeignKeyColumns() != null && o.getForeignKeyColumns() != null) {
            returnValue = this.getForeignKeyColumns().compareToIgnoreCase(o.getForeignKeyColumns());
        }
        if (returnValue == 0 && this.getName() != null && o.getName() != null) {
            returnValue = this.getName().compareToIgnoreCase(o.getName());
        }
        if (returnValue == 0 && this.getPrimaryKeyTable() != null && o.getPrimaryKeyTable() != null) {
            returnValue = this.getPrimaryKeyTable().compareTo(o.getPrimaryKeyTable());
        }

        if (returnValue == 0 && this.getPrimaryKeyColumns() != null && o.getPrimaryKeyColumns() != null) {
            returnValue = this.getPrimaryKeyColumns().compareToIgnoreCase(o.getPrimaryKeyColumns());
        }
        if (returnValue == 0 && this.updateRule != null && o.getUpdateRule() != null)
            returnValue = this.updateRule.compareTo(o.getUpdateRule());
        if (returnValue == 0 && this.deleteRule != null && o.getDeleteRule() != null)
            returnValue = this.deleteRule.compareTo(o.getDeleteRule());
        return returnValue;
    }

    private String toDisplayString(List<String> columnsNames) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (String columnName : columnsNames) {
            i++;
            sb.append(columnName);
            if (i < columnsNames.size()) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }
}
