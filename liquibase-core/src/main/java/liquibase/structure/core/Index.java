package liquibase.structure.core;

import liquibase.structure.AbstractDatabaseObject;
import liquibase.structure.DatabaseObject;
import liquibase.util.StringUtil;

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
            setRelation(new Table(catalogName, schemaName, tableName));
            if ((columns != null) && (columns.length > 0)) {
                setColumns(Arrays.asList(columns));
            }
        }
    }

    @Override
    public DatabaseObject[] getContainingObjects() {
        return new DatabaseObject[] {
        		getRelation()
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
        if (getRelation() == null) {
            return null;
        }
        
        return getRelation().getSchema();
    }

    /**
     * @deprecated Use {@link #getRelation()}
     */
    @Deprecated
	public Table getTable() {
		Relation relation = getRelation();
		if (relation instanceof Table)
		return (Table) relation;
	else
		return null;
	}

    /**
     * @deprecated Use {@link #setRelation(Relation)}
     */
    @Deprecated
	public Index setTable(Table table) {
		return setRelation(table);
    }

    public Relation getRelation() {
    	return getAttribute("table", Relation.class);
    }

    public Index setRelation(Relation relation) {
    	this.setAttribute("table", relation);
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
        column.setRelation(getRelation());
        getColumns().add(column);

        return this;
    }

    public Index setColumns(List<Column> columns) {
        if (getAttribute("table", Object.class) instanceof Table) {
            for (Column column :columns) {
                column.setRelation(getRelation());
            }
        }
        setAttribute("columns", columns);
        return this;
    }

    public String getColumnNames() {
        return StringUtil.join(getColumns(), ", ", new StringUtil.ToStringFormatter());
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
		return StringUtil.join(getAssociatedWith(), ",");
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

        if ((this.getRelation() != null) && (o.getRelation() != null)) {
            returnValue = this.getRelation().compareTo(o.getRelation());
            if ((returnValue == 0) && (this.getRelation().getSchema() != null) && (o.getRelation().getSchema() != null)) {
                returnValue = StringUtil.trimToEmpty(this.getRelation().getSchema().getName()).compareToIgnoreCase(StringUtil.trimToEmpty(o.getRelation().getSchema().getName()));
            }
        }

        if (returnValue == 0) {
            String thisName = StringUtil.trimToEmpty(this.getName());
            String oName = StringUtil.trimToEmpty(o.getName());
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
        if ((getRelation() != null) && (getColumns() != null)) {
            String tableName = getRelation().getName();
            if ((getRelation().getSchema() != null) && (getRelation().getSchema().getName() != null)) {
                tableName = getRelation().getSchema().getName()+"."+tableName;
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