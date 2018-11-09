package liquibase.structure.core;

import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;
import liquibase.resource.ResourceAccessor;
import liquibase.structure.AbstractDatabaseObject;
import liquibase.structure.DatabaseObject;
import liquibase.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UniqueConstraint extends AbstractDatabaseObject {

    public UniqueConstraint() {
        setAttribute("columns", new ArrayList());
        setAttribute("deferrable", false);
        setAttribute("initiallyDeferred", false);
        setAttribute("disabled", false);
		setAttribute("validate", true);
    }

    public UniqueConstraint(String name, String tableCatalog, String tableSchema, String tableName, Column... columns) {
        this();
        setName(name);
        if ((tableName != null) && (columns != null)) {
        	setRelation(new Table(tableCatalog, tableSchema, tableName));
            setColumns(new ArrayList<>(Arrays.asList(columns)));
        }
    }

	@Override
    public DatabaseObject[] getContainingObjects() {
		return getColumns().toArray(new Column[getColumns().size()]);
	}

	@Override
    public String getName() {
		return getAttribute("name", String.class);
	}

	@Override
    public UniqueConstraint setName(String constraintName) {
        this.setAttribute("name", constraintName);
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
	public UniqueConstraint setTable(Table table) {
		return setRelation(table);
    }
    
    public Relation getRelation() {
    	return getAttribute("table", Relation.class);
    }
    
    public UniqueConstraint setRelation(Relation relation) {
    	this.setAttribute("table", relation);
        return this;
    }

	public List<Column> getColumns() {
		return getAttribute("columns", List.class);
	}

    public UniqueConstraint setColumns(List<Column> columns) {
        setAttribute("columns", columns);
        if (getAttribute("table", Object.class) instanceof Table) {
            for (Column column : getColumns()) {
                column.setRelation(getRelation());
            }
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
		return getAttribute("deferrable", Boolean.class);
	}

	public UniqueConstraint setDeferrable(boolean deferrable) {
		this.setAttribute("deferrable",  deferrable);
        return this;
    }

  /**
   * In Oracle PL/SQL, the VALIDATE keyword defines whether a newly added unique constraint on a 
   * column in a table should cause existing rows to be checked to see if they satisfy the 
   * uniqueness constraint or not. 
   * @return true if ENABLE VALIDATE (this is the default), or false if ENABLE NOVALIDATE.
   */
	public boolean shouldValidate() {
		return getAttribute("validate", true);
	}

  /**
   * @param shouldValidate - if shouldValidate is set to FALSE then the constraint will be created
   * with the 'ENABLE NOVALIDATE' mode. This means the constraint would be created, but that no
   * check will be done to ensure old data has valid constraints - only new data would be checked
   * to see if it complies with the constraint logic. The default state for unique constraints is to
   * have 'ENABLE VALIDATE' set.
   */
	public UniqueConstraint setShouldValidate(boolean shouldValidate) {
		this.setAttribute("validate", shouldValidate);
		return this;
	}

	public boolean isInitiallyDeferred() {
		return getAttribute("initiallyDeferred", Boolean.class);
	}

	public UniqueConstraint setInitiallyDeferred(boolean initiallyDeferred) {
		this.setAttribute("initiallyDeferred", initiallyDeferred);
        return this;
    }

	public String getColumnNames() {
		return StringUtil.join(getColumns(), ", ", new StringUtil.StringUtilFormatter() {
            @Override
            public String toString(Object obj) {
                return ((Column) obj).toString(false);
            }
        });
	}

	public UniqueConstraint setDisabled(boolean disabled) {
		this.setAttribute("disabled", disabled);
        return this;
    }

	public boolean isDisabled() {
		return getAttribute("disabled", Boolean.class);
	}

    public Index getBackingIndex() {
        return getAttribute("backingIndex", Index.class);
    }

    public UniqueConstraint setBackingIndex(Index backingIndex) {
        this.setAttribute("backingIndex", backingIndex);
        return this;

    }

	public UniqueConstraint setClustered(boolean clustered) {
		this.setAttribute("clustered", clustered);
		return this;
	}

	public boolean isClustered() {
		return getAttribute("clustered", false);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if ((o == null) || (getClass() != o.getClass()))
			return false;
		if (null == this.getColumnNames())
			return false;
		UniqueConstraint that = (UniqueConstraint) o;
		boolean result = false;
        result = !((getColumnNames() != null) ? !getColumnNames().equalsIgnoreCase(that.getColumnNames()) : (that
            .getColumnNames() != null)) && (isDeferrable() == that.isDeferrable()) && (isInitiallyDeferred() == that
            .isInitiallyDeferred()) && (isDisabled() == that.isDisabled());
		// Need check for nulls here due to NullPointerException using
		// Postgres
		if (result) {
			if (null == this.getRelation()) {
				result = null == that.getRelation();
			} else if (null == that.getRelation()) {
				result = false;
			} else {
				result = this.getRelation().getName().equals(
						that.getRelation().getName());
			}
		}

		return result;

	}

	@Override
    public int compareTo(Object other) {
        UniqueConstraint o = (UniqueConstraint) other;
		// Need check for nulls here due to NullPointerException using Postgres
		String thisTableName;
		String thatTableName;
        thisTableName = (null == this.getRelation()) ? "" : this.getRelation().getName();
        thatTableName = (null == o.getRelation()) ? "" : o.getRelation().getName();
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
    public void load(ParsedNode parsedNode, ResourceAccessor resourceAccessor) throws ParsedNodeException {
        super.load(parsedNode, resourceAccessor);
    }

    @Override
	public int hashCode() {
		int result = 0;
		if (this.getRelation() != null) {
			result = this.getRelation().hashCode();
		}
		if (this.getName() != null) {
            result = (31 * result) + this.getName().toUpperCase().hashCode();
		}
		if (getColumnNames() != null) {
            result = (31 * result) + getColumnNames().hashCode();
		}
		return result;
	}

	@Override
	public String toString() {
        if (getRelation() == null) {
            return getName();
        } else {
            return getName() + " on " + getRelation().getName() + "("
                    + getColumnNames() + ")";
        }
    }
}
