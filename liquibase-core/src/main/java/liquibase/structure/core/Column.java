package liquibase.structure.core;

import liquibase.structure.AbstractDatabaseObject;
import liquibase.structure.DatabaseObject;

public class Column extends AbstractDatabaseObject {

    public Column() {
        setUnique(false);
        setAutoIncrement(false);
    }

    public Relation getRelation() {
        return getAttribute("relation", Relation.class);
    }

    public DatabaseObject[] getContainingObjects() {
        return new DatabaseObject[] {
                getRelation()
        };
    }

    public Column setRelation(Relation relation) {
        setAttribute("relation", relation);

        return this;
    }


    public Schema getSchema() {
        Relation relation = getRelation();
        if (relation == null) {
            return null;
        }
        return relation.getSchema();
    }

	public String getName() {
        return getAttribute("name", String.class);
    }

    public Column setName(String name) {
        setAttribute("name", name);

        return this;
    }

    public Boolean isNullable() {
        return getAttribute("nullable", Boolean.class);
    }

    public Column setNullable(Boolean nullable) {
        setAttribute("nullable", nullable);

        return this;
    }


    public DataType getType() {
        return getAttribute("type", DataType.class);
    }

    public Column setType(DataType type) {
        setAttribute("type", type);

        return this;
    }

    public Object getDefaultValue() {
        return getAttribute("defaultValue", Object.class);
    }

    public Column setDefaultValue(Object defaultValue) {
        setAttribute("defaultValue", defaultValue);

        return this;
    }

    public boolean isAutoIncrement() {
        return getAttribute("autoIncrement", Boolean.class);
    }

    public void setAutoIncrement(boolean autoIncrement) {
        setAttribute("autoIncrement", autoIncrement);
    }

    @Override
    public String toString() {
        String tableOrViewName = getRelation().getName();
        return tableOrViewName +"."+getName();
    }


    @Override
    public int compareTo(Object other) {
        Column o = (Column) other;
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

            return getName().equalsIgnoreCase(column.getName()) && !(getRelation() != null ? !getRelation().equals(column.getRelation()) : column.getRelation() != null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public int hashCode() {
        try {
            int result;
            result = (getRelation() != null ? getRelation().hashCode() : 0);
            result = 31 * result + getName().toUpperCase().hashCode();
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isUnique() {
        return (Boolean) getAttribute("unique", Boolean.class);
    }

    public Column setUnique(boolean unique) {
        setAttribute("unique", unique);

        return this;
    }

    public String getRemarks() {
        return getAttribute("remarks", String.class);
    }

    public Column setRemarks(String remarks) {
        setAttribute("remarks", remarks);

        return this;
    }
}

