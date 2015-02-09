package liquibase.structure.core;

import liquibase.structure.AbstractDatabaseObject;
import liquibase.structure.DatabaseObject;
import liquibase.structure.ObjectName;
import liquibase.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UniqueConstraint extends AbstractDatabaseObject {

    public UniqueConstraint() {
        set("columns", new ArrayList());
        set("deferrable", false);
        set("initiallyDeferred", false);
        set("disabled", false);
    }

    public UniqueConstraint(String name) {
        setName(name);
    }


    public UniqueConstraint(String name, String tableCatalog, String tableSchema, String tableName, Column... columns) {
        this();
        setName(name);
        if (tableName != null && columns != null) {
            setTable(new Table(tableCatalog, tableSchema, tableName));
            setColumns(new ArrayList<Column>(Arrays.asList(columns)));
        }
    }

	@Override
    public DatabaseObject[] getContainingObjects() {
		return getColumns().toArray(new Column[getColumns().size()]);
	}

    @Override
    public Schema getSchema() {
        if (getTable() == null) {
            return null;
        }

        return getTable().getSchema();
    }

	public Table getTable() {
		return get("table", Table.class);
	}

	public UniqueConstraint setTable(Table table) {
		this.set("table", table);
        return this;
    }

	public List<Column> getColumns() {
		return get("columns", List.class);
	}

    public UniqueConstraint setColumns(List<Column> columns) {
        set("columns", columns);
        for (Column column : getColumns()) {
            column.setRelation(getTable());
        }

        return this;
    }

    public UniqueConstraint addColumn(int position, Column column) {
        if (position >= getColumns().size()) {
            for (int i = getColumns().size()-1; i < position; i++) {
                this.getColumns().add(null);
            }
        }
        this.getColumns().set(position, column);
        return this;
    }

    public boolean isDeferrable() {
		return get("deferrable", Boolean.class);
	}

	public UniqueConstraint setDeferrable(boolean deferrable) {
		this.set("deferrable", deferrable);
        return this;
    }

	public boolean isInitiallyDeferred() {
		return get("initiallyDeferred", Boolean.class);
	}

	public UniqueConstraint setInitiallyDeferred(boolean initiallyDeferred) {
		this.set("initiallyDeferred", initiallyDeferred);
        return this;
    }

	public String getColumnNames() {
		return StringUtils.join(getColumns(), ", ", new StringUtils.StringUtilsFormatter() {
            @Override
            public String toString(Object obj) {
                return ((Column) obj).toString(false);
            }
        });
	}

	public UniqueConstraint setDisabled(boolean disabled) {
		this.set("disabled", disabled);
        return this;
    }

	public boolean isDisabled() {
		return get("disabled", Boolean.class);
	}

    public Index getBackingIndex() {
        return get("backingIndex", Index.class);
    }

    public UniqueConstraint setBackingIndex(Index backingIndex) {
        this.set("backingIndex", backingIndex);
        return this;

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

	@Override
    public int compareTo(Object other) {
        UniqueConstraint o = (UniqueConstraint) other;
		// Need check for nulls here due to NullPointerException using Postgres
		ObjectName thisTableName;
        ObjectName thatTableName;
		thisTableName = null == this.getTable() ? new ObjectName() : this.getTable().getName();
		thatTableName = null == o.getTable() ? new ObjectName() : o.getTable().getName();
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
		if (this.getTable() != null) {
			result = this.getTable().hashCode();
		}
		if (this.getName() != null) {
			result = 31 * result + this.getSimpleName().toUpperCase().hashCode();
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
