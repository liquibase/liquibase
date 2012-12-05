package liquibase.structure.core;

import liquibase.structure.AbstractDatabaseObject;
import liquibase.structure.DatabaseObject;
import liquibase.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class PrimaryKey extends AbstractDatabaseObject {
    private String name;
    private List<String> columnNames = new ArrayList<String>();
    private Table table;
    private boolean certainName = true;
	private String tablespace;
    private Index backingIndex;

    public DatabaseObject[] getContainingObjects() {
        return new DatabaseObject[] {
                table
        };
    }

    public String getName() {
        return name;
    }

    public PrimaryKey setName(String name) {
        this.name = name;
        return this;
    }

    public Schema getSchema() {
        if (table == null) {
            return null;
        }
        return table.getSchema();
    }

    public String getColumnNames() {
        return StringUtils.join(columnNames, ", ");
    }

    public void addColumnName(int position, String columnName) {
        if (position >= columnNames.size()) {
            for (int i = columnNames.size()-1; i < position; i++) {
                this.columnNames.add(null);
            }
        }
        this.columnNames.set(position, columnName);
    }

    public Table getTable() {
        return table;
    }

    public PrimaryKey setTable(Table table) {
        this.table = table;
        return this;
    }


    public int compareTo(Object other) {
        PrimaryKey o = (PrimaryKey) other;
        int returnValue = this.getTable().getName().compareTo(o.getTable().getName());
        if (returnValue == 0) {
            returnValue = this.getColumnNames().compareTo(o.getColumnNames());
        }
//        if (returnValue == 0) {
//            returnValue = this.getName().compareTo(o.getName());
//        }

        return returnValue;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PrimaryKey that = (PrimaryKey) o;

        return !(getColumnNames() != null ? !getColumnNames().equals(that.getColumnNames()) : that.getColumnNames() != null) && !(getTable().getName() != null ? !getTable().getName().equals(that.getTable().getName()) : that.getTable().getName() != null);

    }

    @Override
    public int hashCode() {
        int result;
        result = (getColumnNames() != null ? getColumnNames().hashCode() : 0);
        result = 31 * result + (table.getName() != null ? table.getName().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return getName() + " on " + getTable().getName() + "(" + getColumnNames() + ")";
    }

    public List<String> getColumnNamesAsList() {
        return columnNames;
    }

    public boolean isCertainName() {
        return certainName;
    }

    public void setCertainName(boolean certainName) {
        this.certainName = certainName;
    }

	public String getTablespace() {
		return tablespace;
	}

	public void setTablespace(String tablespace) {
		this.tablespace = tablespace;
	}

    public Index getBackingIndex() {
        return backingIndex;
    }

    public PrimaryKey setBackingIndex(Index backingIndex) {
        this.backingIndex = backingIndex;
        return this;
    }
}