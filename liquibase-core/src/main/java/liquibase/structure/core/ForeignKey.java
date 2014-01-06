package liquibase.structure.core;

import liquibase.structure.AbstractDatabaseObject;
import liquibase.structure.DatabaseObject;
import liquibase.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ForeignKey extends AbstractDatabaseObject{

    @Override
    public DatabaseObject[] getContainingObjects() {

        List<Column> objects = new ArrayList<Column>();
        if (getPrimaryKeyColumns() != null) {
            for (String column : StringUtils.splitAndTrim(getPrimaryKeyColumns(), ",")) {
                objects.add(new Column().setName(column).setRelation(getPrimaryKeyTable()));
            }
        }

        if (getForeignKeyColumns() != null) {
            for (String column : StringUtils.splitAndTrim(getForeignKeyColumns(), ",")) {
                objects.add(new Column().setName(column).setRelation(getForeignKeyTable()));
            }
        }

        return objects.toArray(new DatabaseObject[objects.size()]);
    }

    @Override
    public Schema getSchema() {
        if (getForeignKeyTable() == null) {
            return null;
        }

        return getForeignKeyTable().getSchema();
    }


    public Table getPrimaryKeyTable() {
        return getAttribute("primaryKeyTable", Table.class);
    }

    public ForeignKey setPrimaryKeyTable(Table primaryKeyTable) {
        this.setAttribute("primaryKeyTable",primaryKeyTable);
        return this;
    }

    public String getPrimaryKeyColumns() {
        return getAttribute("primaryKeyColumns", String.class);
    }

    public void addPrimaryKeyColumn(String primaryKeyColumn) {
        if ((this.getPrimaryKeyColumns() == null)
                || (this.getPrimaryKeyColumns().length() == 0)) {
            this.setPrimaryKeyColumns(primaryKeyColumn);
        } else {
            this.setPrimaryKeyColumns(this.getPrimaryKeyColumns() + ", " + primaryKeyColumn);
        }
    }

    public ForeignKey setPrimaryKeyColumns(String primaryKeyColumns) {
        this.setAttribute("primaryKeyColumns", primaryKeyColumns);
        return this;
    }

    public Table getForeignKeyTable() {
        return getAttribute("foreignKeyTable", Table.class);
    }

    public ForeignKey setForeignKeyTable(Table foreignKeyTable) {
        this.setAttribute("foreignKeyTable", foreignKeyTable);
        return this;
    }

    public String getForeignKeyColumns() {
        return getAttribute("foreignKeyColumns", String.class);
    }

    public void addForeignKeyColumn(String foreignKeyColumn) {
        if ((this.getForeignKeyColumns() == null)
                || (this.getForeignKeyColumns().length() == 0)) {
            this.setForeignKeyColumns(foreignKeyColumn);
        } else {
            this.setForeignKeyColumns(this.getForeignKeyColumns() + ", "
                    + foreignKeyColumn);
        }
    }

    public ForeignKey setForeignKeyColumns(String foreignKeyColumns) {
        this.setAttribute("foreignKeyColumns", foreignKeyColumns);
        return this;
    }

    @Override
    public String getName() {
        return getAttribute("name", String.class);
    }

    @Override
    public ForeignKey setName(String name) {
        this.setAttribute("name", name);
        return this;
    }


    @Override
    public String toString() {
        return getName() + "(" + getForeignKeyTable() + "." + getForeignKeyColumns() + " -> " + getPrimaryKeyTable() + "." + getPrimaryKeyColumns() + ")";
    }


    public boolean isDeferrable() {
        return getAttribute("deferrable", Boolean.class);
    }

    public ForeignKey setDeferrable(boolean deferrable) {
        this.setAttribute("deferrable", deferrable);
        return this;
    }


    public boolean isInitiallyDeferred() {
        return getAttribute("initiallyDeferred", Boolean.class);
    }

    public ForeignKey setInitiallyDeferred(boolean initiallyDeferred) {
        this.setAttribute("initiallyDeferred", initiallyDeferred);
        return this;
    }

    public ForeignKey setUpdateRule(ForeignKeyConstraintType rule) {
        this.setAttribute("updateRule", rule);
        return this;
    }

    public ForeignKeyConstraintType getUpdateRule() {
        return getAttribute("updateRule", ForeignKeyConstraintType.class);
    }

    public ForeignKey setDeleteRule(ForeignKeyConstraintType rule) {
        this.setAttribute("deleteRule", rule);
        return this;
    }

    public ForeignKeyConstraintType getDeleteRule() {
        return getAttribute("deleteRule", ForeignKeyConstraintType.class);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ForeignKey that = (ForeignKey) o;

        if (getForeignKeyColumns() == null) {
            return this.getName().equalsIgnoreCase(that.getName());
        }

        return (getForeignKeyColumns() != null && that.getForeignKeyColumns() != null && getForeignKeyColumns().equalsIgnoreCase(that.getForeignKeyColumns()))
                && (getForeignKeyTable() != null && that.getForeignKeyTable() != null && getForeignKeyTable().equals(that.getForeignKeyTable()))
                && (getPrimaryKeyColumns() != null && that.getPrimaryKeyColumns() != null && getPrimaryKeyColumns().equalsIgnoreCase(that.getPrimaryKeyColumns()))
                && (getPrimaryKeyTable() != null && that.getPrimaryKeyTable() != null && getPrimaryKeyTable().equals(that.getPrimaryKeyTable()));
    }

    @Override
    public int hashCode() {
        int result = 0;
        if (getPrimaryKeyTable() != null) {
            result = getPrimaryKeyTable().hashCode();
        }
        if (getPrimaryKeyColumns() != null) {
            result = 31 * result + getPrimaryKeyColumns().toUpperCase().hashCode();
        }

        if (getForeignKeyTable() != null) {
            result = 31 * result + getForeignKeyTable().hashCode();
        }

        if (getForeignKeyColumns() != null) {
            result = 31 * result + getForeignKeyColumns().toUpperCase().hashCode();
        }

        return result;
    }


    @Override
    public int compareTo(Object other) {
        ForeignKey o = (ForeignKey) other;
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
        if (returnValue == 0 && this.getUpdateRule() != null && o.getUpdateRule() != null)
            returnValue = this.getUpdateRule().compareTo(o.getUpdateRule());
        if (returnValue == 0 && this.getDeleteRule() != null && o.getDeleteRule() != null)
            returnValue = this.getDeleteRule().compareTo(o.getDeleteRule());
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

    public Index getBackingIndex() {
        return getAttribute("backingIndex", Index.class);
    }

    public ForeignKey setBackingIndex(Index backingIndex) {
        this.setAttribute("backingIndex", backingIndex);
        return this;
    }
}
