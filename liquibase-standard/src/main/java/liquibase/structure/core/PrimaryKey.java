package liquibase.structure.core;

import liquibase.structure.AbstractDatabaseObject;
import liquibase.structure.DatabaseObject;
import liquibase.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PrimaryKey extends AbstractDatabaseObject {

    public PrimaryKey() {
        setAttribute("columns", new ArrayList<>());
    }

    public PrimaryKey(String name, String tableCatalogName, String tableSchemaName, String tableName, Column... columns) {
        this();
        setName(name);
        if (tableName != null) {
            Table table = new Table(tableCatalogName, tableSchemaName, tableName);

            if (columns != null) {
                setAttribute("columns", Arrays.asList(columns));
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
        return StringUtil.join(getColumns(), ", ", obj -> ((Column) obj).getName());
    }

    /**
     * Adds a new column to the column list of this PrimaryKey. The first column has the position 0.
     * If you specify a position that is greater than the number of columns present, undefined
     * columns (NULL expressions) will be added as padding. If a position that is already
     * occupied by a column is specified, that column will be replaced.
     *
     * @param position the position where to insert or replace the column
     * @param column   the new column
     * @return a reference to the updated PrimaryKey object.
     */
    public PrimaryKey addColumn(int position, Column column) {
        if (position >= getColumns().size()) {
            for (int i = getColumns().size()-1; i < position; i++) {
                this.getColumns().add(null);
            }
        }
        this.getColumns().set(position, column);
        return this;
    }

    /**
     * Returns the Table object this PrimaryKey belongs to.
     *
     * @return the Table object, or null if not initialized yet.
     */
    public Table getTable() {
        return getAttribute("table", Table.class);
    }

    /**
     * Sets the Table object this PrimaryKey belongs to.
     *
     * @param table the table object to set as the container for this PrimaryKey
     * @return the updated object
     */
    public PrimaryKey setTable(Table table) {
        this.setAttribute("table", table);
        return this;
    }


    @Override
    public int compareTo(Object other) {
        PrimaryKey o = (PrimaryKey) other;
        int returnValue = this.getTable().compareTo(o.getTable());
        if (returnValue == 0) {
            returnValue = this.getColumnNames().compareTo(o.getColumnNames());
        }

        return returnValue;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if ((o == null) || (getClass() != o.getClass())) return false;

        PrimaryKey that = (PrimaryKey) o;

        return !((getColumnNames() != null) ? !getColumnNames().equals(that.getColumnNames()) : (that.getColumnNames
            () != null)) && !((getTable() != null) ? !getTable().equals(that.getTable()) : (that.getTable() != null));

    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public String toString() {
        if (getTable() == null) {
            return getName();
        } else {
            String tableName = getTable().getName();
            if (getTable().getSchema() != null) {
                tableName = getTable().getSchema().getName()+"."+tableName;
            }
            return getName() + " on " + tableName + "(" + getColumnNames() + ")";
        }
    }

    public List<Column> getColumns() {
        return getAttribute("columns", List.class);
    }

    public List<String> getColumnNamesAsList() {
        List<String> names = new ArrayList<>();
        for (Column col : getColumns()) {
            names.add(col.getName());
        }
        return names;
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

    /**
     * @param shouldValidate - if shouldValidate is set to FALSE then the constraint will be created
     * with the 'ENABLE NOVALIDATE' mode. This means the constraint would be created, but that no
     * check will be done to ensure old data has valid primary keys - only new data would be checked
     * to see if it complies with the constraint logic. The default state for primary keys is to
     * have 'ENABLE VALIDATE' set.
     */
    public PrimaryKey setShouldValidate(boolean shouldValidate) {
        this.setAttribute("validate", shouldValidate);
        return this;
    }
}
