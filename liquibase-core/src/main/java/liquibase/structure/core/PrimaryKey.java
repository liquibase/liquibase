package liquibase.structure.core;

import liquibase.structure.AbstractDatabaseObject;
import liquibase.structure.DatabaseObject;
import liquibase.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PrimaryKey extends AbstractDatabaseObject {

    public PrimaryKey() {
        setAttribute("columnNames", new ArrayList());
    }

    public PrimaryKey(String name, String tableCatalogName, String tableSchemaName, String tableName, String... columnNames) {
        this();
        setName(name);
        if (tableName != null) {
            Table table = new Table(tableCatalogName, tableSchemaName, tableName);

            if (columnNames != null) {
                setAttribute("columnNames", Arrays.asList(columnNames));
            }

            setTable(table);
        }
    }

    @Override
    public DatabaseObject[] getContainingObjects() {
        return new DatabaseObject[] {
                getTable()
        };
    }

    @Override
    public String getName() {
        return getAttribute("name", String.class);
    }

    @Override
    public PrimaryKey setName(String name) {
        this.setAttribute("name", name);
        return this;
    }

    @Override
    public Schema getSchema() {
        if (getTable() == null) {
            return null;
        }
        return getTable().getSchema();
    }

    public String getColumnNames() {
        return StringUtils.join(getColumnNamesAsList(), ", ");
    }

    public PrimaryKey addColumnName(int position, String columnName) {
        if (position >= getColumnNamesAsList().size()) {
            for (int i = getColumnNamesAsList().size()-1; i < position; i++) {
                this.getColumnNamesAsList().add(null);
            }
        }
        this.getColumnNamesAsList().set(position, columnName);
        return this;
    }

    public Table getTable() {
        return getAttribute("table", Table.class);
    }

    public PrimaryKey setTable(Table table) {
        this.setAttribute("table", table);
        return this;
    }


    @Override
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
        result = 31 * result + (getTable().getName() != null ? getTable().getName().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        if (getTable() == null) {
            return getName();
        } else {
            return getName() + " on " + getTable().getName() + "(" + getColumnNames() + ")";
        }
    }

    public List<String> getColumnNamesAsList() {
        return getAttribute("columnNames", List.class);
    }

    public boolean isCertainName() {
        return getAttribute("certainName", Boolean.class);
    }

    public PrimaryKey setCertainName(boolean certainName) {
        setAttribute("certainName", certainName);
        return this;
    }

	public String getTablespace() {
		return getAttribute("tablespace",String.class);
	}

	public PrimaryKey setTablespace(String tablespace) {
        setAttribute("tablespace", tablespace);
        return this;
	}

    public Index getBackingIndex() {
        return getAttribute("backingIndex", Index.class);
    }

    public PrimaryKey setBackingIndex(Index backingIndex) {
        setAttribute("backingIndex", backingIndex);
        return this;
    }
}