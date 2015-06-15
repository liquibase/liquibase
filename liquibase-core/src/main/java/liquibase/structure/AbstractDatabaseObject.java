package liquibase.structure;

import liquibase.AbstractExtensibleObject;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;
import liquibase.resource.ResourceAccessor;
import liquibase.serializer.LiquibaseSerializable;
import liquibase.structure.core.Column;
import liquibase.structure.core.Schema;
import liquibase.util.ISODateFormat;
import liquibase.util.ObjectUtil;
import liquibase.util.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractDatabaseObject  extends AbstractExtensibleObject implements DatabaseObject {

    private String snapshotId;

    public ObjectName name;

    @Override
    public String getObjectTypeName() {
        return StringUtils.lowerCaseFirst(getClass().getSimpleName());
    }

    public AbstractDatabaseObject() {
    }

    public AbstractDatabaseObject(ObjectName name) {
        setName(name);
    }

    public String getSimpleName() {
        ObjectName name = getName();
        if (name == null) {
            return null;
        } else {
            return name.name;
        }
    }

    public ObjectName getName() {
        return name;
    }

    @Override
    public <T> T setName(ObjectName name) {
        this.name = name;

        return (T) this;
    }

    @Override
    public String getSnapshotId() {
        return snapshotId;
    }

    @Override
    public void setSnapshotId(String snapshotId) {
        if (this.snapshotId != null) {
            throw new UnexpectedLiquibaseException("snapshotId already set");
        }
        this.snapshotId = snapshotId;
    }

    @Override
    public boolean snapshotByDefault() {
        return true;
    }

    @Override
    public int compareTo(Object o) {
        return this.getName().compareTo(((AbstractDatabaseObject) o).getName());
    }

    @Override
    public String getSerializedObjectName() {
        return getObjectTypeName();
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_SNAPSHOT_NAMESPACE;
    }

    @Override
    public String getSerializableFieldNamespace(String field) {
        return getSerializedObjectNamespace();
    }

    @Override
    public Set<String> getSerializableFields() {
        TreeSet<String> fields = new TreeSet<String>(getAttributeNames());
        fields.add("snapshotId");
        return fields;
    }

    @Override
    public Object getSerializableFieldValue(String field) {
        if (field.equals("snapshotId")) {
            return snapshotId;
        }
        if (!getAttributeNames().contains(field)) {
            throw new UnexpectedLiquibaseException("Unknown field " + field);
        }
            Object value = get(field, Object.class);
            if (value instanceof Schema) {
                Schema clone = new Schema(((Schema) value).getName());
                clone.setSnapshotId(((DatabaseObject) value).getSnapshotId());
                return clone;
            } else if (value instanceof DatabaseObject) {
                try {
                    DatabaseObject clone = (DatabaseObject) value.getClass().newInstance();
                    clone.setName(((DatabaseObject) value).getName());
                    clone.setSnapshotId(((DatabaseObject) value).getSnapshotId());
                    return clone;
                } catch (Exception e) {
                    throw new UnexpectedLiquibaseException(e);
                }
            }
        return value;
    }

    @Override
    public LiquibaseSerializable.SerializationType getSerializableFieldType(String field) {
        if (getSerializableFieldValue(field) instanceof DatabaseObject) {
            return LiquibaseSerializable.SerializationType.NAMED_FIELD;
        } else {
            return LiquibaseSerializable.SerializationType.NAMED_FIELD;
        }
    }

    @Override
    public void load(ParsedNode parsedNode, ResourceAccessor resourceAccessor) throws ParsedNodeException {
        for (ParsedNode child : parsedNode.getChildren()) {
            String name = child.getName();
            if (name.equals("snapshotId")) {
                this.snapshotId = child.getValue(String.class);
                continue;
            }

            Class propertyType = ObjectUtil.getPropertyType(this, name);
            if (propertyType != null && Collection.class.isAssignableFrom(propertyType) && !(child.getValue() instanceof Collection)) {
                if (!this.getAttributeNames().contains(name)) {
                    this.set(name, new ArrayList<Column>());
                }
                this.get(name, List.class).add(child.getValue());
            } else {
                Object childValue = child.getValue();
                if (childValue != null && childValue instanceof String) {
                    Matcher matcher = Pattern.compile("(.*)!\\{(.*)\\}").matcher((String) childValue);
                    if (matcher.matches()) {
                        String stringValue = matcher.group(1);
                        try {
                            Class<?> aClass = Class.forName(matcher.group(2));
                            if (Date.class.isAssignableFrom(aClass)) {
                                Date date = new ISODateFormat().parse(stringValue);
                                childValue = aClass.getConstructor(long.class).newInstance(date.getTime());
                            } else if (Enum.class.isAssignableFrom(aClass)) {
                                childValue = Enum.valueOf((Class<? extends Enum>) aClass, stringValue);
                            } else {
                                childValue = aClass.getConstructor(String.class).newInstance(stringValue);
                            }
                        } catch (Exception e) {
                            throw new UnexpectedLiquibaseException(e);
                        }
                    }
                }

                this.set(name, childValue);
            }
        }
    }

    @Override
    public ParsedNode serialize() {
        throw new RuntimeException("TODO");
    }

    @Override
    public String toString() {
        return getName().toString();
    }

    @Override
    public int hashCode() {
        return StringUtils.trimToEmpty(getName().toString()).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof DatabaseObject
                && StringUtils.trimToEmpty(getName().toString()).equals(((DatabaseObject) obj).getName().toString());
    }
}
