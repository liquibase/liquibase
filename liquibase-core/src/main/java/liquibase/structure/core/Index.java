package liquibase.structure.core;

import liquibase.database.Database;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.structure.AbstractDatabaseObject;
import liquibase.structure.DatabaseObject;
import liquibase.util.StringUtils;

import java.util.*;

public class Index extends AbstractDatabaseObject {

	/** Marks Index as associated with Primary Key [PK] */
	public final static String MARK_PRIMARY_KEY = "primaryKey";
	/** Marks Index as associated with Foreign Key [FK] */
	public final static String MARK_FOREIGN_KEY = "foreignKey";
	/** Marks Index as associated with Unique Constraint [UC] */
	public final static String MARK_UNIQUE_CONSTRAINT = "uniqueConstraint";

    public Index() {
        setAttribute("columns", new ArrayList<String>());
        setAttribute("associatedWith", new HashSet<String>());
    }

    public Index(String indexName) {
        this();
        setName(indexName);
    }

    public Index(String indexName, String catalogName, String schemaName, String tableName, Column... columns) {
        this();
        setName(indexName);
        if (tableName != null) {
            setTable(new Table(catalogName, schemaName, tableName));
            if (columns != null && columns.length > 0) {
                setColumns(Arrays.asList(columns));
            }
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
    public Index setName(String name) {
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

    public Table getTable() {
        return getAttribute("table", Table.class);
    }

    public Index setTable(Table table) {
        this.setAttribute("table", table);
        return this;
    }

	public String getTablespace() {
		return getAttribute("tablespace", String.class);
	}

	public Index setTablespace(String tablespace) {
        this.setAttribute("tablespace", tablespace);
        return this;
	}

    public List<Column> getColumns() {
        return getAttribute("columns", List.class);
    }

    public Index addColumn(Column column) {
        column.setRelation(getTable());
        getColumns().add(column);

        return this;
    }

    public Index setColumns(List<Column> columns) {
        for (Column column :columns) {
            column.setRelation(getTable());
        }
        setAttribute("columns", columns);
        return this;
    }

    public String getColumnNames() {
        return StringUtils.join(getColumns(), ", ", new StringUtils.ToStringFormatter());
    }

    public Index setUnique(Boolean value) {
        this.setAttribute("unique", value);
        return this;
    }

    public Boolean isUnique() {
        return getAttribute("unique", Boolean.class);
    }

	public Set<String> getAssociatedWith() {
		return getAttribute("associatedWith", Set.class);
	}

	public String getAssociatedWithAsString() {
		return StringUtils.join(getAssociatedWith(), ",");
	}

	public void addAssociatedWith(String item) {
		getAssociatedWith().add(item);
	}

	public boolean isAssociatedWith(String keyword) {
		return getAssociatedWith().contains(keyword);
	}


    public Boolean getClustered() {
        return getAttribute("clustered", Boolean.class);
    }

    public Index setClustered(Boolean clustered) {
        return (Index) setAttribute("clustered", clustered);
    }

//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//
//        Index index = (Index) o;
//
//        if (name != null ? !name.equals(index.name) : index.name != null) return false;
//
//        return true;
//    }
//
//    @Override
//    public int hashCode() {
//        return name != null ? name.hashCode() : 0;
//    }

    @Override
    public int compareTo(Object other) {
        Index o = (Index) other;
        int returnValue = this.getTable().getName().compareTo(o.getTable().getName());

        if (returnValue == 0) {
            String thisName = StringUtils.trimToEmpty(this.getName());
            String oName = StringUtils.trimToEmpty(o.getName());
            returnValue = thisName.compareTo(oName);
        }

        //We should not have two indexes that have the same name and tablename
        /*if (returnValue == 0) {
        	returnValue = this.getColumnName().compareTo(o.getColumnName());
        }*/


        return returnValue;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!(obj instanceof Index)) {
            return false;
        }

        return this.compareTo(obj) == 0;
    }

    @Override
    public String toString() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(getName());
        if (this.isUnique() != null && !this.isUnique()) {
            stringBuffer.append(" unique ");
        }
        if (getTable() != null && getColumns() != null) {
            stringBuffer.append(" on ").append(getTable().getName());
            if (getColumns() != null && getColumns().size() > 0) {
                stringBuffer.append("(");
                for (Column column : getColumns()) {
                    stringBuffer.append(column.toString(false)).append(", ");
                }
                stringBuffer.delete(stringBuffer.length() - 2, stringBuffer.length());
                stringBuffer.append(")");
            } else {
                stringBuffer.append("()");
            }
        }
        return stringBuffer.toString();
    }
}