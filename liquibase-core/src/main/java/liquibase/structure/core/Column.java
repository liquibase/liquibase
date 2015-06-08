package liquibase.structure.core;

import liquibase.change.ColumnConfig;
import liquibase.change.ConstraintsConfig;
import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;
import liquibase.resource.ResourceAccessor;
import liquibase.structure.AbstractDatabaseObject;
import liquibase.structure.DatabaseObject;
import liquibase.structure.ObjectName;
import liquibase.util.StringUtils;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

public class Column extends AbstractDatabaseObject {

    public Relation relation;
    public Boolean computed;
    public Boolean descending;
    public Boolean certainDataType;
    public DataType type;
    public AutoIncrementInformation autoIncrementInformation;
    public Boolean nullable;
    public Object defaultValue;
    public String remarks;

    public Column() {
    }

    public Column(ObjectName columnName) {
        super(columnName);
    }

    public Column(String columnName) {
        super(columnName);
    }

    public Column(Class<? extends Relation> relationType, ObjectName tableName, String columnName) {
        if (Table.class.isAssignableFrom(relationType)) {
            this.relation = new Table(tableName);
        } else if (View.class.isAssignableFrom(relationType)) {
            this.relation = new View(tableName);
        }
        setName(columnName);
    }

    public Column(Class<? extends Relation> relationType, String catalogName, String schemaName, String tableName, String columnName) {
        if (Table.class.isAssignableFrom(relationType)) {
            this.relation = new Table(catalogName, schemaName, tableName);
        } else if (View.class.isAssignableFrom(relationType)) {
            this.relation = new View(catalogName, schemaName, tableName);
        }
        setName(columnName);
    }

    public Column(ColumnConfig columnConfig) {
        setName(columnConfig.getName());
        this.descending = columnConfig.getDescending();
        this.type = new DataType(columnConfig.getType());

        if (columnConfig.getDefaultValue() != null) {
            this.defaultValue = columnConfig.getDefaultValueObject();
        }

        if (columnConfig.isAutoIncrement() != null && columnConfig.isAutoIncrement()) {
            this.autoIncrementInformation = new AutoIncrementInformation(columnConfig.getStartWith(), columnConfig.getIncrementBy());
        }

        ConstraintsConfig constraints = columnConfig.getConstraints();
        if (constraints != null) {
            this.nullable = constraints.isNullable();
        }

        this.remarks = columnConfig.getRemarks();
    }

    @Override
    public DatabaseObject[] getContainingObjects() {
        return new DatabaseObject[]{
                relation
        };
    }

    @Override
    public Schema getSchema() {
        if (relation == null) {
            return null;
        }
        return relation.getSchema();
    }

    public Column setName(String name, boolean computed) {
        setName(name);
        this.computed = computed;

        return this;
    }

    public boolean isAutoIncrement() {
        return autoIncrementInformation != null;
    }

    public String toString(boolean includeRelation) {
        if (includeRelation) {
            return toString();
        } else {
            return getName().toShortString()  + (descending != null && descending ? " DESC" : "");
        }
    }

    @Override
    public String toString() {
        String name = getName().toShortString();
        if (relation == null) {
            return name + (descending != null && descending ? " DESC" : "");
        } else {
            return relation.getName().toString()+"." + name + (descending != null && descending ? " DESC" : "");
        }
    }


    @Override
    public int compareTo(Object other) {
        Column o = (Column) other;
        try {
            //noinspection UnusedAssignment
            int returnValue = 0;
            if (this.relation != null && o.relation == null) {
                return 1;
            } else if (this.relation == null && o.relation != null) {
                return -1;
            } else {
                returnValue = this.relation.compareTo(o.relation);
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
        if (!(o instanceof Column)) {
            return false;
        }
        return this.toString().equals(o.toString());
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    public boolean isDataTypeDifferent(Column otherColumn) {
        if (!this.certainDataType || !otherColumn.certainDataType) {
            return false;
        } else {
            return !this.type.equals(otherColumn.type);
        }
    }

    @SuppressWarnings({"SimplifiableIfStatement"})
    public boolean isNullabilityDifferent(Column otherColumn) {
        if (this.nullable == null && otherColumn.nullable == null) {
            return false;
        }
        if (this.nullable == null && otherColumn.nullable != null) {
            return true;
        }
        if (this.nullable != null && otherColumn.nullable == null) {
            return true;
        }
        return !this.nullable.equals(otherColumn.nullable);
    }

    public boolean isDifferent(Column otherColumn) {
        return isDataTypeDifferent(otherColumn) || isNullabilityDifferent(otherColumn);
    }


    public static Column fromName(String columnName) {
        columnName = columnName.trim();
        Boolean descending = null;
        if (columnName.matches("(?i).*\\s+DESC")) {
            columnName = columnName.replaceFirst("(?i)\\s+DESC$", "");
            descending = true;
        } else if (columnName.matches("(?i).*\\s+ASC")) {
            columnName = columnName.replaceFirst("(?i)\\s+ASC$", "");
            descending = false;
        }
        Column column = new Column(columnName);
        column.descending = descending;
        return column;
    }

    public static Column[] arrayFromNames(String columnNames) {
        if (columnNames == null) {
            return null;
        }

        List<String> columnNameList = StringUtils.splitAndTrim(columnNames, ",");
        Column[] returnArray = new Column[columnNameList.size()];
        for (int i = 0; i < columnNameList.size(); i++) {
            returnArray[i] = fromName(columnNameList.get(i));
        }
        return returnArray;
    }

    public static List<Column> listFromNames(String columnNames) {
        if (columnNames == null) {
            return null;
        }
        return Arrays.asList(arrayFromNames(columnNames));
    }

    @Override
    public void load(ParsedNode parsedNode, ResourceAccessor resourceAccessor) throws ParsedNodeException {
        super.load(parsedNode, resourceAccessor);
        ParsedNode typeNode = parsedNode.getChild(null, "type");
        if (typeNode != null) {
            DataType type = new DataType();
            type.load(typeNode, resourceAccessor);
            this.type = type;
        }
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

