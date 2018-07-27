package liquibase.structure;

import liquibase.configuration.LiquibaseConfiguration;
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

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractDatabaseObject implements DatabaseObject {

    private Map<String, Object> attributes = new HashMap<String, Object>();

    private String snapshotId;

    @Override
    public String getObjectTypeName() {
        return StringUtils.lowerCaseFirst(getClass().getSimpleName());
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
        AbstractDatabaseObject that = (AbstractDatabaseObject) o;
        if (this.getSchema() != null && that.getSchema() != null) {
            if (shouldIncludeCatalogInSpecification()) {
                String thisCatalogName = this.getSchema().getCatalogName();
                String thatCatalogName = that.getSchema().getCatalogName();

                if (thisCatalogName != null && thatCatalogName != null) {
                    int compare = thisCatalogName.compareToIgnoreCase(thatCatalogName);
                    if (compare != 0) {
                        return compare;
                    }
                } else if (thisCatalogName != null) {
                    return 1;
                } else if (thatCatalogName != null) {
                    return -1;
                } // if they are both null, it will continue with rest
            }
            // now compare schema name
            int compare = StringUtils.trimToEmpty(this.getSchema().getName()).compareToIgnoreCase(StringUtils.trimToEmpty(that.getSchema().getName()));
            if (compare != 0) {
                return compare;
            }
        }

        String thisName = this.getName();
        String thatName = that.getName();
        if (thisName != null && thatName != null) {
            return thisName.compareTo(thatName);
        } else if (thisName != null) {
            return 1;
        } else if (thatName != null) {
            return -1;
        }
        return 0;
    }

    @Override
    public Set<String> getAttributes() {
        return attributes.keySet();
    }

    @Override
    public <T> T getAttribute(String attribute, Class<T> type) {
        return (T) attributes.get(attribute);
    }

    @Override
    public <T> T getAttribute(String attribute, T defaultValue) {
        T value = (T) attributes.get(attribute);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    @Override
    public DatabaseObject setAttribute(String attribute, Object value) {
        if (value == null) {
            attributes.remove(attribute);
        } else {
            attributes.put(attribute, value);
        }
        return this;
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
        TreeSet<String> fields = new TreeSet<String>(attributes.keySet());
        fields.add("snapshotId");
        return fields;
    }

    @Override
    public Object getSerializableFieldValue(String field) {
        if (field.equals("snapshotId")) {
            return snapshotId;
        }
        if (!attributes.containsKey(field)) {
            throw new UnexpectedLiquibaseException("Unknown field " + field);
        }
        Object value = attributes.get(field);
        try {
            if (value instanceof Schema) {
                Schema clone = new Schema(((Schema) value).getCatalogName(), ((Schema) value).getName());
                clone.setSnapshotId(((DatabaseObject) value).getSnapshotId());
                return clone;
            } else if (value instanceof DatabaseObject) {
                DatabaseObject clone = (DatabaseObject) value.getClass().newInstance();
                clone.setName(((DatabaseObject) value).getName());
                clone.setSnapshotId(((DatabaseObject) value).getSnapshotId());
                return clone;
            }
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
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
                if (this.attributes.get(name) == null) {
                    this.setAttribute(name, new ArrayList<Column>());
                }
                this.getAttribute(name, List.class).add(child.getValue());
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

                this.attributes.put(name, childValue);
            }
        }
    }

    @Override
    public ParsedNode serialize() {
        throw new RuntimeException("TODO");
    }

    @Override
    public String toString() {
        return getName();
    }

    /**
     * Convenience method to check if the object types should consider catalog name
     * also during comparision (equals(), hashcode() and compareTo())
     *
     * @return
     */
    public boolean shouldIncludeCatalogInSpecification() {
        return LiquibaseConfiguration.getInstance().shouldIncludeCatalogInSpecification();
    }
}
