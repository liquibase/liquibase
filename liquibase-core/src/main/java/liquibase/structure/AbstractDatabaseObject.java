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

import java.io.DataOutput;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractDatabaseObject  extends AbstractExtensibleObject implements DatabaseObject {

    private String snapshotId;
    public ObjectReference container;
    public String name;

    @Override
    public String getObjectTypeName() {
        return StringUtils.lowerCaseFirst(getClass().getSimpleName());
    }

    public AbstractDatabaseObject() {
    }

    public AbstractDatabaseObject(String name) {
        this.name = name;
    }

    public AbstractDatabaseObject(ObjectReference nameAndContainer) {
        this.name = nameAndContainer.name;
        this.container = nameAndContainer.container;
    }

    public AbstractDatabaseObject(ObjectReference container, String name) {
        this.container = container;
        this.name = name;
    }



    /**
     * Returns the name. Marked final so subclasses don't change business logic and make it not match get("name")
     */
    public final String getName() {
        return name;
    }

    /**
     * Returns the schema. Marked final so subclasses don't change business logic and make it not match get("schema")
     */
    @Override
    public final ObjectReference getContainer() {
        return container;
    }

    /**
     * Returns the snapshotId. Marked final so subclasses don't change business logic and make it not match get("snapshotId")
     */
    @Override
    public final String getSnapshotId() {
        return snapshotId;
    }

    @Override
    public boolean snapshotByDefault() {
        return true;
    }

    @Override
    public int compareTo(Object o) {
        if (o == null || !(o instanceof DatabaseObject)) {
            return 1;
        }
        return this.toReference().compareTo(((DatabaseObject) o).toReference());
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


    /**
     * Convenience method for subclasses to use in the standard toString(). Returns name, prefixed with container if it exists
     */
    protected String toString(ObjectReference container, String name) {
        String string = name;
        if (container != null) {
            string = container.toString()+"."+name;
        }
        return string;
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
                Schema clone = new Schema(((ObjectReference) value).name);
                clone.set("snapshotId", (((DatabaseObject) value).getSnapshotId()));
                return clone;
            } else if (value instanceof DatabaseObject) {
                try {
                    DatabaseObject clone = (DatabaseObject) value.getClass().newInstance();
                    clone.set("name", ((DatabaseObject) value).getName());
                    clone.set("snapshotId", ((DatabaseObject) value).getSnapshotId());
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
        return toReference().toString();
    }

    @Override
    public int hashCode() {
        return toReference().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof DatabaseObject
                && this.toReference().equals(((DatabaseObject) obj).toReference());
    }

    @Override
    public ObjectReference toReference() {
        return new ObjectReference(getClass(), container, name);
    }
}
