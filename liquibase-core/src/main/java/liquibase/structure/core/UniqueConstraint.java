package liquibase.structure.core;

import liquibase.structure.AbstractDatabaseObject;
import liquibase.structure.DatabaseObject;
import liquibase.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class UniqueConstraint extends AbstractDatabaseObject implements Comparable<UniqueConstraint> {
	private String name;
	private Table table;
	private List<String> columns = new ArrayList<String>();

	private boolean deferrable;
	private boolean initiallyDeferred;
	private boolean disabled;
	
    private Index backingIndex;

	public DatabaseObject[] getContainingObjects() {
		List<DatabaseObject> columns = new ArrayList<DatabaseObject>();
		for (String column : this.columns) {
			columns.add(new Column().setName(column).setRelation(table));
		}

		return columns.toArray(new DatabaseObject[columns.size()]);
	}

	public String getName() {
		return name;
	}

	public void setName(String constraintName) {
		this.name = constraintName;
	}

    public Schema getSchema() {
        if (table == null) {
            return null;
        }

        return table.getSchema();
    }

	public Table getTable() {
		return table;
	}

	public void setTable(Table table) {
		this.table = table;
	}

	public List<String> getColumns() {
		return columns;
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

	public String getColumnNames() {
		return StringUtils.join(columns, ", ");
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	public boolean isDisabled() {
		return disabled;
	}

    public Index getBackingIndex() {
        return backingIndex;
    }

    public void setBackingIndex(Index backingIndex) {
        this.backingIndex = backingIndex;
    }

    @Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		if (null == this.getColumnNames())
			return false;
		UniqueConstraint that = (UniqueConstraint) o;
		boolean result = false;
		result = !(getColumnNames() != null ? !getColumnNames()
				.equalsIgnoreCase(that.getColumnNames()) : that
				.getColumnNames() != null)
				&& isDeferrable() == that.isDeferrable()
				&& isInitiallyDeferred() == that.isInitiallyDeferred()
				&& isDisabled() == that.isDisabled();
		// Need check for nulls here due to NullPointerException using
		// Postgres
		if (result) {
			if (null == this.getTable()) {
				result = null == that.getTable();
			} else if (null == that.getTable()) {
				result = false;
			} else {
				result = this.getTable().getName().equals(
						that.getTable().getName());
			}
		}

		return result;

	}

	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(UniqueConstraint o) {
		// Need check for nulls here due to NullPointerException using Postgres
		String thisTableName;
		String thatTableName;
		thisTableName = null == this.getTable() ? "" : this.getTable()
				.getName();
		thatTableName = null == o.getTable() ? "" : o.getTable().getName();
		int returnValue = thisTableName.compareTo(thatTableName);
		if (returnValue == 0) {
			returnValue = this.getName().compareTo(o.getName());
		}
		if (returnValue == 0) {
			returnValue = this.getColumnNames().compareTo(o.getColumnNames());
		}
		return returnValue;
	}

	@Override
	public int hashCode() {
		int result = 0;
		if (this.table != null) {
			result = this.table.hashCode();
		}
		if (this.name != null) {
			result = 31 * result + this.name.toUpperCase().hashCode();
		}
		if (getColumnNames() != null) {
			result = 31 * result + getColumnNames().hashCode();
		}
		return result;
	}

	@Override
	public String toString() {
		return getName() + " on " + getTable().getName() + "("
				+ getColumnNames() + ")";
	}
}
