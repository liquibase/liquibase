package liquibase.structure.core;

import liquibase.structure.AbstractDatabaseObject;
import liquibase.structure.DatabaseObject;
import liquibase.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PrimaryKey extends AbstractDatabaseObject {

    public PrimaryKey() {
        set("columns", new ArrayList());
    }

    public PrimaryKey(String name) {
        setName(name);
    }

    public PrimaryKey(String name, String tableCatalogName, String tableSchemaName, String tableName, Column... columns) {
        this();
        setName(name);
        if (tableName != null) {
            Table table = new Table(tableCatalogName, tableSchemaName, tableName);

            if (columns != null) {
                set("columns", Arrays.asList(columns));
                for (Column column : getColumns()) {
                    column.setRelation(table);
                }
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
    public Schema getSchema() {
        if (getTable() == null) {
            return null;
        }
        return getTable().getSchema();
    }

    public String getColumnNames() {
        return StringUtils.join(getColumns(), ", ", new StringUtils.StringUtilsFormatter() {
            @Override
            public String toString(Object obj) {
                return ((Column) obj).toString(false);
            }
        });
    }

    public PrimaryKey addColumn(int position, Column column) {
        if (position >= getColumns().size()) {
            for (int i = getColumns().size()-1; i < position; i++) {
                this.getColumns().add(null);
            }
        }
        this.getColumns().set(position, column);
        return this;
    }

    public Table getTable() {
        return get("table", Table.class);
    }

    public PrimaryKey setTable(Table table) {
        this.set("table", table);
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
            return getName().toShortString();
        } else {
            return getName() + " on " + getTable().getName() + "(" + getColumnNames() + ")";
        }
    }

    public List<Column> getColumns() {
        return get("columns", List.class);
    }

    public List<String> getColumnNamesAsList() {
        List<String> names = new ArrayList<String>();
        for (Column col : getColumns()) {
            names.add(col.getName().toShortString());
        }
        return names;
    }

    public boolean isCertainName() {
        return get("certainName", Boolean.class);
    }

    public PrimaryKey setCertainName(boolean certainName) {
        set("certainName", certainName);
        return this;
    }

	public String getTablespace() {
		return get("tablespace", String.class);
	}

	public PrimaryKey setTablespace(String tablespace) {
        set("tablespace", tablespace);
        return this;
	}

    public Index getBackingIndex() {
        return get("backingIndex", Index.class);
    }

    public PrimaryKey setBackingIndex(Index backingIndex) {
        set("backingIndex", backingIndex);
        return this;
    }
}