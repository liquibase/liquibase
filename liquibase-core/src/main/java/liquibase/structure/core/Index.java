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

    private String name;
    private Table table;
	private String tablespace;
    private Boolean unique;
    private List<String> columns = new ArrayList<String>();
    private String filterCondition;
	// Contain associations of index
	// for example: foreignKey, primaryKey or uniqueConstraint
	private Set<String> associatedWith = new HashSet<String>();

	public DatabaseObject[] getContainingObjects() {
        return new DatabaseObject[] {
                table
        };
    }

    public String getName() {
        return name;
    }

    public Index setName(String name) {
        this.name = name;
        return this;
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

    public Index setTable(Table table) {
        this.table = table;
        return this;
    }

	public String getTablespace() {
		return tablespace;
	}

	public Index setTablespace(String tablespace) {
		this.tablespace = tablespace;
        return this;
	}

    public List<String> getColumns() {
        return columns;
    }

    public String getColumnNames() {
        return StringUtils.join(columns, ", ");
    }

    public String getFilterCondition() {
        return filterCondition;
    }

    public Index setFilterCondition(String filterCondition) {
        this.filterCondition = filterCondition;
        return this;
    }

    public Index setUnique(Boolean value) {
        this.unique = value;
        return this;
    }

    public Boolean isUnique() {
        return this.unique;
    }

	public Set<String> getAssociatedWith() {
		return associatedWith;
	}

	public String getAssociatedWithAsString() {
		return StringUtils.join(associatedWith, ",");
	}

	public void addAssociatedWith(String item) {
		associatedWith.add(item);
	}

	public boolean isAssociatedWith(String keyword) {
		return associatedWith.contains(keyword);
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
        int returnValue = this.table.getName().compareTo(o.table.getName());

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
    public String toString() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(getName());
        if (this.unique != null && !this.unique) {
            stringBuffer.append(" unique ");
        }
        if (table != null && columns != null) {
            stringBuffer.append(" on ").append(table.getName());
            if (columns != null && columns.size() > 0) {
                stringBuffer.append("(");
                for (String column : columns) {
                    stringBuffer.append(column).append(", ");
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