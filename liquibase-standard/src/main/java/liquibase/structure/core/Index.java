package liquibase.structure.core;

import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;
import liquibase.resource.ResourceAccessor;
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
	public Index setTable(Relation table) {
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
    
    public List<String> getIncludedColumns() {
    	List<String> toRet =  getAttribute("includedColumns", List.class);
    	return (toRet==null)?Collections.EMPTY_LIST:toRet;
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

    @Override
    public Object getSerializableFieldValue(String field) {
        //
        // For columns within an Index, we now represent
        // all the columns as actual Column objects with
        // the forIndex flag set
        //
        if (field != null && field.equals("columns")) {
            List<Object> returnList = new ArrayList<>();
            for (Column column : getColumns()) {
                Column c = new Column();
                c.setName(column.getName());
                c.setDescending(column.getDescending());
                c.setComputed(column.getComputed());
                c.setForIndex(true);
                returnList.add(c);
            }
            return returnList;
        }
        return super.getSerializableFieldValue(field);
    }

    @Override
    public void load(ParsedNode parsedNode, ResourceAccessor resourceAccessor) throws ParsedNodeException {
        super.load(parsedNode, resourceAccessor);
        ParsedNode columns = parsedNode.getChild(null, "columns");
        if (columns != null) {
            List<ParsedNode> nodes = columns.getChildren(null, "column");
            for (ParsedNode node : nodes) {
                Column column = new Column();
                column.load(node, resourceAccessor);
                column.setName((String) node.getChildren(null, "name").get(0).getValue());
                column.setDescending(node.getChildValue(null, "descending", Boolean.class));
                column.setComputed(node.getChildValue(null, "computed", Boolean.class));
                getColumns().add(column);
            }
            //
            // Clear out any null objects which may have been added before
            // by the super class load. We check to see if the list only
            // contains Column objects in order to support older snapshots
            // which may have both Column objects and reference strings
            //
            if (!nodes.isEmpty() && allColumnObjects(getColumns())) {
                List<Column> newList = new ArrayList<>();
                for (Column column : getColumns()) {
                    if (column == null) {
                        continue;
                    }
                    newList.add(column);
                }
                setColumns(newList);
            }
        }
    }

    //
    // Make sure this list only contains Column objects
    //
    private boolean allColumnObjects(List columns) {
        for (Object object : columns) {
            if (object instanceof String) {
                return false;
            }
        }
        return true;
    }
    public Boolean getClustered() {
        return getAttribute("clustered", Boolean.class);
    }

    public Index setClustered(Boolean clustered) {
        return (Index) setAttribute("clustered", clustered);
    }

    public String getUsing() {
        return getAttribute("using", String.class);
    }

    public Index setUsing(String using) {
        return (Index) setAttribute("using", using);
    }

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
        StringBuilder result = new StringBuilder();
        result.append( (getName() == null) ? "(unnamed index)" : getName());
        if ((this.isUnique() != null) && this.isUnique()) {
            result.append(" UNIQUE ");
        }
        if ((getRelation() != null) && (getColumns() != null)) {
            String tableName = getRelation().getName();
            if ((getRelation().getSchema() != null) && (getRelation().getSchema().getName() != null)) {
                tableName = getRelation().getSchema().getName()+"."+tableName;
            }
            result.append(" ON ").append(tableName);
            if ((getColumns() != null) && !getColumns().isEmpty()) {
                result.append("(");
                for (Column column : getColumns()) {
                    if (column == null)
                        // 0th entry of an index column list might be null if index only has
                        // regular columns!
                        result.append("(null), ");
                    else
                        result.append(column.toString(false)).append(", ");
                }
                result.delete(result.length() - 2, result.length());
                result.append(")");
            } else {
                result.append("()");
            }
        }
        return result.toString();
    }
}
