package liquibase.structure.core;

import liquibase.AbstractExtensibleObject;
import liquibase.change.ColumnConfig;
import liquibase.change.ConstraintsConfig;
import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;
import liquibase.resource.ResourceAccessor;
import liquibase.serializer.AbstractLiquibaseSerializable;
import liquibase.serializer.LiquibaseSerializable;
import liquibase.structure.AbstractDatabaseObject;
import liquibase.structure.DatabaseObject;
import liquibase.structure.ObjectName;
import liquibase.util.StringUtils;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class Column extends AbstractDatabaseObject {

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

    public Column(ObjectName name) {
        super(name);
    }

    public Column(ColumnConfig columnConfig) {
        super(new ObjectName(columnConfig.getName()));
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
        return null; // new DatabaseObject[]{ relation };
    }

//    @Override
    public Schema getSchema() {
//        if (relation == null) {
            return null;
//        }
//        return relation.getSchema();
    }

    public Column setName(String name, boolean computed) {
        setName(new ObjectName(name));
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
        return getName().toString() + (descending != null && descending ? " DESC" : "");
    }


    @Override
    public int compareTo(Object other) {
        return this.getName().compareTo(((Column) other).getName());
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
        Column column = new Column(new ObjectName(columnName));
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

        ParsedNode autoIncrementInformation = parsedNode.getChild(null, "autoIncrementInformation");
        if (autoIncrementInformation != null) {
            AutoIncrementInformation info = new AutoIncrementInformation();
//            info.load(autoIncrementInformation, resourceAccessor);
//            setAutoIncrementInformation(info);
        }

    }

    public String getRelationName() {
        return name.asList(2).get(1);
    }

    public String getSchemaName() {
        return name.asList(3).get(2);
    }

    public String getCatalogName() {
        return name.asList(4).get(3);
    }

    public static class AutoIncrementInformation extends AbstractExtensibleObject implements LiquibaseSerializable {
        public BigInteger startWith;
        public BigInteger incrementBy;

        public AutoIncrementInformation() {
        }

        public AutoIncrementInformation(Number startWith, Number incrementBy) {
            this.startWith = startWith == null ? null : BigInteger.valueOf(startWith.longValue());
            this.incrementBy = incrementBy == null ? null : BigInteger.valueOf(incrementBy.longValue());
        }

        @Override
        public String toString() {
            return "AUTO INCREMENT START WITH " + startWith + " INCREMENT BY " + incrementBy;
        }

        @Override
        public String getSerializedObjectName() {
            return "autoIncrementInformation";
        }

        @Override
        public String getSerializedObjectNamespace() {
            return STANDARD_CHANGELOG_NAMESPACE;
        }

        @Override
        public void load(ParsedNode parsedNode, ResourceAccessor resourceAccessor) throws ParsedNodeException {
//            this.startWith = (BigInteger) convertEscaped(parsedNode.getChildValue(null, "startWith"));
//            this.incrementBy = (BigInteger) convertEscaped(parsedNode.getChildValue(null, "incrementBy"));
        }

        @Override
        public Set<String> getSerializableFields() {
            return null;
        }

        @Override
        public Object getSerializableFieldValue(String field) {
            return null;
        }

        @Override
        public SerializationType getSerializableFieldType(String field) {
            return null;
        }

        @Override
        public String getSerializableFieldNamespace(String field) {
            return null;
        }

        @Override
        public ParsedNode serialize() throws ParsedNodeException {
            return null;
        }
    }
}

