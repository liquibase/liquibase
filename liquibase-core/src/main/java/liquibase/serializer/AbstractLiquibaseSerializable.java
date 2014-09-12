package liquibase.serializer;

import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.parser.NamespaceDetails;
import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;
import liquibase.resource.ResourceAccessor;
import liquibase.util.ObjectUtil;

import java.util.*;

public abstract class AbstractLiquibaseSerializable implements LiquibaseSerializable {

    private Set<String> serializableFields;

    public void load(ParsedNode parsedNode, ResourceAccessor resourceAccessor) throws ParsedNodeException {
        for (ParsedNode childNode : parsedNode.getChildren()) {
            if (!shouldAutoLoad(childNode)) {
                continue;
            }
            try {
                if (this.getSerializableFields().contains(childNode.getName())) {
                    Object value = childNode.getValue();
                    if (value != null) {
                        value = value.toString();
                    }
                    ObjectUtil.setProperty(this, childNode.getName(), (String) value);
                }
            } catch (Exception e) {
                throw new ParsedNodeException("Error setting property", e);
            }
        }

        if (parsedNode.getValue() != null) {
            for (String field : this.getSerializableFields()) {
                if (this.getSerializableFieldType(field) == SerializationType.DIRECT_VALUE) {
                    ObjectUtil.setProperty(this, field, parsedNode.getValue(String.class));
                }
            }
        }
    }

    protected boolean shouldAutoLoad(ParsedNode node) {
        return true;
    }

    @Override
    public ParsedNode serialize() throws ParsedNodeException {
        ParsedNode node = new ParsedNode(null, getSerializedObjectName());
        for (String field : getSerializableFields()) {
            Object fieldValue = getSerializableFieldValue(field);
            fieldValue = serializeValue(fieldValue);
            if (fieldValue == null) {
                continue;
            }

            SerializationType type = getSerializableFieldType(field);
            if (type == SerializationType.DIRECT_VALUE) {
                node.setValue(fieldValue);
            } else if (type == SerializationType.NAMED_FIELD || type == SerializationType.NESTED_OBJECT) {
                if (fieldValue instanceof ParsedNode) {
                    node.addChild((ParsedNode) fieldValue);
                } else {
                    node.addChild(new ParsedNode(null, field).setValue(fieldValue));
                }
            } else {
                throw new UnexpectedLiquibaseException("Unknown type: "+type);
            }
        }
        return node;
    }

    @Override
    public Set<String> getSerializableFields() {
        return ReflectionSerializer.getInstance().getFields(this);
    }

    @Override
    public Object getSerializableFieldValue(String field) {
        return ReflectionSerializer.getInstance().getValue(this, field);
    }

    @Override
    public SerializationType getSerializableFieldType(String field) {
        return SerializationType.NAMED_FIELD;
    }

    protected Object serializeValue(Object value) throws ParsedNodeException {
        if (value instanceof Collection) {
            List returnList = new ArrayList();
            for (Object obj : (Collection) value) {
                Object objValue = serializeValue(obj);
                if (objValue != null) {
                    returnList.add(objValue);
                }
            }
            if (((Collection) value).size() == 0) {
                return null;
            } else {
                return returnList;
            }
        } else if (value instanceof LiquibaseSerializable) {
            return ((LiquibaseSerializable) value).serialize();
        } else {
            return value;
        }
    }


    @Override
    public String getSerializableFieldNamespace(String field) {
        return getSerializedObjectNamespace();
    }

}
