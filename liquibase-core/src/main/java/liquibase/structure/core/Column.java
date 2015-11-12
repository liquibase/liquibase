package liquibase.structure.core;

import liquibase.AbstractExtensibleObject;
import liquibase.change.ColumnConfig;
import liquibase.change.ConstraintsConfig;
import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;
import liquibase.resource.ResourceAccessor;
import liquibase.serializer.LiquibaseSerializable;
import liquibase.structure.AbstractDatabaseObject;
import liquibase.structure.DatabaseObject;
import liquibase.structure.ObjectReference;
import liquibase.util.StringUtils;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class Column extends AbstractDatabaseObject {

    public ObjectReference table;
    public DataType type;
    public AutoIncrementInformation autoIncrementInformation;
    public Boolean nullable;
    public Object defaultValue;
    public String remarks;
    public Boolean virtual;

    public Column() {
    }

    public Column(String name) {
        super(name);
    }

    public Column(ObjectReference nameAndContainer) {
        super(nameAndContainer);
    }

    public Column(ObjectReference container, String name) {
        super(container, name);
    }

    public Column(ObjectReference table, String columnName, DataType type, Boolean nullable) {
        this(table, columnName);
        this.type = type;
        this.nullable = nullable;
    }

    public Column(ObjectReference table, String columnName, String type) {
        this(table, columnName, DataType.parse(type));
    }

    public Column(ObjectReference table, String columnName, DataType type) {
        this(table, columnName, type, null);
    }

    public Column(ColumnConfig columnConfig) {
        super(columnConfig.getName());
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

    public Column setName(String name, boolean virtual) {
        this.name = name;
        this.virtual = virtual;

        return this;
    }

    public boolean isAutoIncrement() {
        return autoIncrementInformation != null;
    }

    public String toString(boolean includeRelation) {
        if (includeRelation) {
            return toString();
        } else {
            return name;
        }
    }

    @Override
    public int compareTo(Object other) {
        return this.getName().compareTo(((Column) other).getName());
    }

    @Override
    public void load(ParsedNode parsedNode, ResourceAccessor resourceAccessor) throws ParsedNodeException {
        super.load(parsedNode, resourceAccessor);
        ParsedNode typeNode = parsedNode.getChild(null, "type");
        if (typeNode != null) {
            DataType type = new DataType();
//            type.load(typeNode, resourceAccessor);
            this.type = type;
        }

        ParsedNode autoIncrementInformation = parsedNode.getChild(null, "autoIncrementInformation");
        if (autoIncrementInformation != null) {
            AutoIncrementInformation info = new AutoIncrementInformation();
//            info.load(autoIncrementInformation, resourceAccessor);
//            setAutoIncrementInformation(info);
        }

    }

    /**
     * For a column reference, "container" is the table.
     */
    public static class ColumnReference extends ObjectReference {

        public ColumnReference() {
        }

        public ColumnReference(Class<? extends DatabaseObject> type, ObjectReference container, String... names) {
            super(type, container, names);
        }

        public ColumnReference(ObjectReference container, String... names) {
            super(container, names);
        }

        public ColumnReference(String... names) {
            super(names);
        }

        public ObjectReference getRelation() {
            return container;
        }

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

