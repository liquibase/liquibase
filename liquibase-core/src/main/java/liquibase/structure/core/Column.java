package liquibase.structure.core;

import liquibase.structure.AbstractDatabaseObject;
import liquibase.structure.DatabaseObject;

import java.math.BigInteger;

public class Column extends AbstractDatabaseObject {

    private String name;

    public Column() {
    }

    public Column(Class<? extends Relation> relationType, String catalogName, String schemaName, String tableName, String columnName) {
        if (Table.class.isAssignableFrom(relationType)) {
            this.setRelation(new Table(catalogName, schemaName, tableName));
        } else if (View.class.isAssignableFrom(relationType)) {
            this.setRelation(new View(catalogName, schemaName, tableName));
        }
        setName(columnName);
    }

    public Relation getRelation() {
        return getAttribute("relation", Relation.class);
    }

    @Override
    public DatabaseObject[] getContainingObjects() {
        return new DatabaseObject[] {
                getRelation()
        };
    }

    public Column setRelation(Relation relation) {
        setAttribute("relation", relation);

        return this;
    }


    @Override
    public Schema getSchema() {
        Relation relation = getRelation();
        if (relation == null) {
            return null;
        }
        return relation.getSchema();
    }

	@Override
    public String getName() {
        return name;
    }

    @Override
    public Column setName(String name) {
        this.name = name;
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
       return getAutoIncrementInformation() != null;
    }

    public AutoIncrementInformation getAutoIncrementInformation() {
        return getAttribute("autoIncrementInformation", AutoIncrementInformation.class);
    }

    public void setAutoIncrementInformation(AutoIncrementInformation autoIncrementInformation) {
        setAttribute("autoIncrementInformation", autoIncrementInformation);
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


    public boolean isCertainDataType() {
        return getAttribute("certainDataType", Boolean.class);
    }

    public Column setCertainDataType(boolean certainDataType) {
        setAttribute("certainDataType", certainDataType);

        return this;
    }

    public String getRemarks() {
        return getAttribute("remarks", String.class);
    }

    public Column setRemarks(String remarks) {
        setAttribute("remarks", remarks);

        return this;
    }

    public static class AutoIncrementInformation {
        private BigInteger startWith;
        private BigInteger incrementBy;

        public AutoIncrementInformation() {
            this(1,1);
        }

        public AutoIncrementInformation(Number startWith, Number incrementBy) {
            this.startWith = BigInteger.valueOf(startWith.longValue());
            this.incrementBy = BigInteger.valueOf(incrementBy.longValue());
        }

        public BigInteger getStartWith() {
            return startWith;
        }

        public BigInteger getIncrementBy() {
            return incrementBy;
        }

        @Override
        public String toString() {
            return "AUTO INCREMENT START WITH "+startWith+" INCREMENT BY "+incrementBy;
        }
    }
}

