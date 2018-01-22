package liquibase.structure.core;

import liquibase.structure.AbstractDatabaseObject;
import liquibase.structure.DatabaseObject;
import liquibase.util.StringUtils;

import java.util.*;

public class Index extends AbstractDatabaseObject {

	/** Marks Index as associated with Primary Key [PK] */
    public static final String MARK_PRIMARY_KEY = "primaryKey";
	/** Marks Index as associated with Foreign Key [FK] */
    public static final String MARK_FOREIGN_KEY = "foreignKey";
	/** Marks Index as associated with Unique Constraint [UC] */
    public static final String MARK_UNIQUE_CONSTRAINT = "uniqueConstraint";

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
            if ((columns != null) && (columns.length > 0)) {
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

    public Relation getTable() {
        return getAttribute("table", Relation.class);
    }

    public Index setTable(Relation table) {
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
        if (getAttribute("table", Object.class) instanceof Table) {
            for (Column column :columns) {
                column.setRelation(getTable());
            }
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
        int returnValue = 0;

        if ((this.getTable() != null) && (o.getTable() != null)) {
            returnValue = this.getTable().compareTo(o.getTable());
            if ((returnValue == 0) && (this.getTable().getSchema() != null) && (o.getTable().getSchema() != null)) {
                returnValue = StringUtils.trimToEmpty(this.getTable().getSchema().getName()).compareToIgnoreCase(StringUtils.trimToEmpty(o.getTable().getSchema().getName()));
            }
        }

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

    /**
     * (Try to) provide a human-readable name for the index.
     * @return A (hopefully) human-readable name
     */
    @Override
    public String toString() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append( (getName() == null) ? "(unnamed index)" : getName());
        if ((this.isUnique() != null) && this.isUnique()) {
            stringBuffer.append(" UNIQUE ");
        }
        if ((getTable() != null) && (getColumns() != null)) {
            String tableName = getTable().getName();
            if ((getTable().getSchema() != null) && (getTable().getSchema().getName() != null)) {
                tableName = getTable().getSchema().getName()+"."+tableName;
            }
            stringBuffer.append(" ON ").append(tableName);
            if ((getColumns() != null) && !getColumns().isEmpty()) {
                stringBuffer.append("(");
                for (Column column : getColumns()) {
                    if (column == null)
                        // 0th entry of an index column list might be null if index only has
                        // regular columns!
                        stringBuffer.append("(null), ");
                    else
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