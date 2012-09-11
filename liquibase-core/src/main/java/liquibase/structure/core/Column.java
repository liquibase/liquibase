package liquibase.structure.core;

import liquibase.structure.DatabaseObject;
import liquibase.structure.DatabaseObjectImpl;

public class Column extends DatabaseObjectImpl implements Comparable<Column> {
    private Relation relation;
    private String name;
    private Boolean nullable;
    private DataType type;
    private Object defaultValue;
    private boolean primaryKey = false;
    private boolean unique = false;
    private boolean autoIncrement = false;

    private boolean certainDataType = true;
    private String remarks;

	// used for PK's index configuration
	private String tablespace;

    public Relation getRelation() {
        return relation;
    }

    public DatabaseObject[] getContainingObjects() {
        return new DatabaseObject[] {
                getRelation()
        };
    }

    public Column setRelation(Relation relation) {
        this.relation = relation;

        return this;
    }


	public String getTablespace() {
		return tablespace;
	}

    public Schema getSchema() {
        Relation relation = getRelation();
        if (relation == null) {
            return null;
        }
        return relation.getSchema();
    }

    public Column setTablespace(String tablespace) {
		this.tablespace = tablespace;
		return  this;
	}

	public String getName() {
        return name;
    }

    public Column setName(String name) {
        this.name = name;

        return this;
    }

    public Boolean isNullable() {
        return nullable;
    }

    public Column setNullable(Boolean nullable) {
        this.nullable = nullable;

        return this;
    }


    public DataType getType() {
        return type;
    }

    public Column setType(DataType type) {
        this.type = type;

        return this;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public Column setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;

        return this;
    }

    public boolean isAutoIncrement() {
        return autoIncrement;
    }

    public void setAutoIncrement(boolean autoIncrement) {
        this.autoIncrement = autoIncrement;
    }

    @Override
    public String toString() {
        String tableOrViewName = relation.getName();
        return tableOrViewName +"."+getName();
    }


    public int compareTo(Column o) {
        try {
            //noinspection UnusedAssignment
            int returnValue = 0;
            if (this.getRelation() != null && o.getRelation() == null) {
                return 1;
            } else if (this.getRelation() == null && o.getRelation() != null) {
                return -1;
            } else {
                returnValue = this.getRelation().compareTo(o.getRelation());
            }

            if (returnValue == 0) {
                returnValue = this.getName().compareTo(o.getName());
            }

            return returnValue;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public boolean equals(Object o) {
        try {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Column column = (Column) o;

            return name.equalsIgnoreCase(column.name) && !(relation != null ? !relation.equals(column.relation) : column.relation != null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public int hashCode() {
        try {
            int result;
            result = (relation != null ? relation.hashCode() : 0);
            result = 31 * result + name.toUpperCase().hashCode();
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isUnique() {
        return unique;
    }

    public Column setUnique(boolean unique) {
        this.unique = unique;

        return this;
    }

    public boolean isDataTypeDifferent(Column otherColumn) {
        if (!this.isCertainDataType() || !otherColumn.isCertainDataType()) {
            return false;
        } else {
            return !this.getType().equals(otherColumn.getType());
        }
    }

    @SuppressWarnings({"SimplifiableIfStatement"})
    public boolean isNullabilityDifferent(Column otherColumn) {
        if (this.isNullable() == null && otherColumn.isNullable() == null) {
            return false;
        }
        if (this.isNullable() == null && otherColumn.isNullable() != null) {
            return true;
        }
        if (this.isNullable() != null && otherColumn.isNullable() == null) {
            return true;
        }
        return !this.isNullable().equals(otherColumn.isNullable());
    }

    public boolean isDifferent(Column otherColumn) {
        return isDataTypeDifferent(otherColumn) || isNullabilityDifferent(otherColumn);
    }


    public boolean isPrimaryKey() {
        return primaryKey;
    }

    public Column setPrimaryKey(boolean primaryKey) {
        this.primaryKey = primaryKey;

        return this;
    }

    public boolean isCertainDataType() {
        return certainDataType;
    }

    public Column setCertainDataType(boolean certainDataType) {
        this.certainDataType = certainDataType;

        return this;
    }

    public String getRemarks() {
        return remarks;
    }

    public Column setRemarks(String remarks) {
        this.remarks = remarks;

        return this;
    }
}

