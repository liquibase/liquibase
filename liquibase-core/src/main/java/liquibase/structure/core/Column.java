package liquibase.structure.core;

import liquibase.change.ColumnConfig;
import liquibase.change.ConstraintsConfig;
import liquibase.structure.AbstractDatabaseObject;
import liquibase.structure.DatabaseObject;
import liquibase.util.StringUtils;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

public class Column extends AbstractDatabaseObject {

    private String name;
    private Boolean computed;

    public Column() {
    }

    public Column(String columnName) {
        setName(columnName);
    }

    public Column(Class<? extends Relation> relationType, String catalogName, String schemaName, String tableName, String columnName) {
        if (Table.class.isAssignableFrom(relationType)) {
            this.setRelation(new Table(catalogName, schemaName, tableName));
        } else if (View.class.isAssignableFrom(relationType)) {
            this.setRelation(new View(catalogName, schemaName, tableName));
        }
        setName(columnName);
    }

    public Column(ColumnConfig columnConfig) {
        setName(columnConfig.getName());
        setType(new DataType(columnConfig.getType()));

        if (columnConfig.getDefaultValue() != null) {
            setDefaultValue(columnConfig.getDefaultValueObject());
        }

        if (columnConfig.isAutoIncrement() != null && columnConfig.isAutoIncrement()) {
            setAutoIncrementInformation(new AutoIncrementInformation(columnConfig.getStartWith(), columnConfig.getIncrementBy()));
        }

        ConstraintsConfig constraints = columnConfig.getConstraints();
        if (constraints != null) {
            setNullable(constraints.isNullable());
        }

        setRemarks(columnConfig.getRemarks());
    }

    public Relation getRelation() {
        return getAttribute("relation", Relation.class);
    }

    @Override
    public DatabaseObject[] getContainingObjects() {
        return new DatabaseObject[]{
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

    public Column setName(String name, boolean computed) {
        setName(name);
        setComputed(computed);

        return this;
    }



    public Boolean getComputed() {
        return computed;
    }

    public Column setComputed(Boolean computed) {
        this.computed = computed;
        setAttribute("computed", computed);

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


    public String toString(boolean includeRelation) {
        if (includeRelation) {
            return toString();
        } else {
            return getName();
        }
    }

    @Override
    public String toString() {
        if (getComputed() != null && getComputed()) {
            return getName();
        } else {
            if (getRelation() == null) {
                return getName();
            } else {
                String tableOrViewName = getRelation().getName();
                return tableOrViewName + "." + getName();
            }
        }
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
                returnValue = this.toString().compareTo(o.toString());
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

            return toString().equalsIgnoreCase(column.toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public int hashCode() {
        return toString().toUpperCase().hashCode();
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

    public static Column[] arrayFromNames(String columnNames) {
        if (columnNames == null) {
            return null;
        }

        List<String> columnNameList = StringUtils.splitAndTrim(columnNames, ",");
        Column[] returnArray = new Column[columnNameList.size()];
        for (int i=0; i<columnNameList.size(); i++) {
            returnArray[i] = new Column(columnNameList.get(i));
        }
        return returnArray;
    }

    public static List<Column> listFromNames(String columnNames) {
        if (columnNames == null) {
            return null;
        }
        return Arrays.asList(arrayFromNames(columnNames));
    }


    public static class AutoIncrementInformation {
        private BigInteger startWith;
        private BigInteger incrementBy;

        public AutoIncrementInformation() {
            this(1, 1);
        }

        public AutoIncrementInformation(Number startWith, Number incrementBy) {
            this.startWith = startWith == null ? null : BigInteger.valueOf(startWith.longValue());
            this.incrementBy = incrementBy == null ? null : BigInteger.valueOf(incrementBy.longValue());
        }

        public BigInteger getStartWith() {
            return startWith;
        }

        public BigInteger getIncrementBy() {
            return incrementBy;
        }

        @Override
        public String toString() {
            return "AUTO INCREMENT START WITH " + startWith + " INCREMENT BY " + incrementBy;
        }
    }
}

