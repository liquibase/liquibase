package liquibase.structure.core;

import liquibase.structure.AbstractDatabaseObject;
import liquibase.structure.DatabaseObject;

import java.util.List;

public class ForeignKey extends AbstractDatabaseObject{
    private Table primaryKeyTable;
    private String primaryKeyColumns;

    private Table foreignKeyTable;
    private String foreignKeyColumns;

    private String name;

    private boolean deferrable;
    private boolean initiallyDeferred;

	// Some databases supports creation of FK with referention to column marked as unique, not primary
	// If FK referenced to such unique column this option should be set to false
	private boolean referencesUniqueColumn = false;

    private ForeignKeyConstraintType updateRule;
    private ForeignKeyConstraintType deleteRule;
    
    private Index backingIndex;

    public DatabaseObject[] getContainingObjects() {
        return new DatabaseObject[] {
                new Column()
                        .setName(getPrimaryKeyColumns())
                        .setRelation(getPrimaryKeyTable()),
                new Column()
                        .setName(getForeignKeyColumns())
                        .setRelation(getForeignKeyTable())

        };
    }

    public Schema getSchema() {
        if (foreignKeyTable == null) {
            return null;
        }

        return foreignKeyTable.getSchema();
    }


    public Table getPrimaryKeyTable() {
        return primaryKeyTable;
    }

    public ForeignKey setPrimaryKeyTable(Table primaryKeyTable) {
        this.primaryKeyTable = primaryKeyTable;
        return this;
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

    public ForeignKey setPrimaryKeyColumns(String primaryKeyColumns) {
        this.primaryKeyColumns = primaryKeyColumns;
        return this;
    }

    public Table getForeignKeyTable() {
        return foreignKeyTable;
    }

    public ForeignKey setForeignKeyTable(Table foreignKeyTable) {
        this.foreignKeyTable = foreignKeyTable;
        return this;
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

    public ForeignKey setForeignKeyColumns(String foreignKeyColumns) {
        this.foreignKeyColumns = foreignKeyColumns;
        return this;
    }

    public String getName() {
        return name;
    }

    public ForeignKey setName(String name) {
        this.name = name;
        return this;
    }


    @Override
    public String toString() {
        return getName() + "(" + getForeignKeyTable() + "." + getForeignKeyColumns() + " -> " + getPrimaryKeyTable() + "." + getPrimaryKeyColumns() + ")";
    }


    public boolean isDeferrable() {
        return deferrable;
    }

    public ForeignKey setDeferrable(boolean deferrable) {
        this.deferrable = deferrable;
        return this;
    }


    public boolean isInitiallyDeferred() {
        return initiallyDeferred;
    }

    public ForeignKey setInitiallyDeferred(boolean initiallyDeferred) {
        this.initiallyDeferred = initiallyDeferred;
        return this;
    }

    public ForeignKey setUpdateRule(ForeignKeyConstraintType rule) {
        this.updateRule = rule;
        return this;
    }

    public ForeignKeyConstraintType getUpdateRule() {
        return this.updateRule;
    }

    public ForeignKey setDeleteRule(ForeignKeyConstraintType rule) {
        this.deleteRule = rule;
        return this;
    }

    public ForeignKeyConstraintType getDeleteRule() {
        return this.deleteRule;
    }

	public boolean getReferencesUniqueColumn() {
		return referencesUniqueColumn;
	}

	public ForeignKey setReferencesUniqueColumn(boolean referencesUniqueColumn) {
		this.referencesUniqueColumn = referencesUniqueColumn;
        return this;
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ForeignKey that = (ForeignKey) o;

        if (getForeignKeyColumns() == null) {
            return this.getName().equalsIgnoreCase(that.getName());
        }

        return getForeignKeyColumns().equalsIgnoreCase(that.getForeignKeyColumns())
                && foreignKeyTable.equals(that.foreignKeyTable)
                && getPrimaryKeyColumns().equalsIgnoreCase(that.getPrimaryKeyColumns())
                && primaryKeyTable.equals(that.primaryKeyTable)
		        && referencesUniqueColumn == that.getReferencesUniqueColumn();
    }

    @Override
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

    public Index getBackingIndex() {
        return backingIndex;
    }

    public ForeignKey setBackingIndex(Index backingIndex) {
        this.backingIndex = backingIndex;
        return this;
    }
}
