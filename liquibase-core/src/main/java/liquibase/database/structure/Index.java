package liquibase.database.structure;

import liquibase.util.StringUtils;

import java.util.*;

public class Index implements DatabaseObject, Comparable<Index> {

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

    public void setName(String name) {
        this.name = name;
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

	public String getTablespace() {
		return tablespace;
	}

	public void setTablespace(String tablespace) {
		this.tablespace = tablespace;
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

    public void setFilterCondition(String filterCondition) {
        this.filterCondition = filterCondition;
    }

    public void setUnique(Boolean value) {
        this.unique = value;
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

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Index index = (Index) o;
        boolean equals = getColumnNames().equals(index.getColumnNames());

        if (this.unique == null && index.isUnique() == null) {
            //continue check
        } else if (this.unique == null && index.isUnique() != null) {
            equals = false;
        } else  if (this.unique != null && index.isUnique() == null) {
            equals = false;
        } else if (!this.unique.equals(index.isUnique())) {
            equals = false;
        }

        return equals && table.getName().equalsIgnoreCase(index.table.getName()) && getName().equals(index.getName());

    }

    @Override
    public int hashCode() {
        int result;
        result = table.getName().toUpperCase().hashCode();
        result = 31 * result + columns.hashCode();
        result = 31 * result + (unique == null ? 2 : unique ? 1 : 0);
        return result;
    }

    public int compareTo(Index o) {
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
            if (columns != null) {
                stringBuffer.append("(");
                for (String column : columns) {
                    stringBuffer.append(column).append(", ");
                }
                stringBuffer.delete(stringBuffer.length() - 2, stringBuffer.length());
                stringBuffer.append(")");
            }
        }
        return stringBuffer.toString();
    }
}