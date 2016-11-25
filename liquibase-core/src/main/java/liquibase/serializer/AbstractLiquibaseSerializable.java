package liquibase.serializer;

import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;
import liquibase.resource.ResourceAccessor;
import liquibase.util.ObjectUtil;

import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
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
                    Class dataTypeClass = this.getSerializableFieldDataTypeClass(childNode.getName());
                    if (Collection.class.isAssignableFrom(dataTypeClass)) {
                        Type[] dataTypeClassParameters = getSerializableFieldDataTypeClassParameters(childNode.getName());
                        if (dataTypeClassParameters.length == 1) {
                            Class collectionType = null;
                            if (dataTypeClassParameters[0] instanceof Class) {
                                collectionType = (Class) dataTypeClassParameters[0];
                            } else if (dataTypeClassParameters[0] instanceof ParameterizedType) {
                                collectionType = (Class) ((ParameterizedType) dataTypeClassParameters[0]).getRawType();
                            }
                            if (collectionType != null
                                    && LiquibaseSerializable.class.isAssignableFrom(collectionType)
                                    && !collectionType.isInterface()
                                    && !Modifier.isAbstract(collectionType.getModifiers())) {

                                String elementName = ((LiquibaseSerializable) collectionType.newInstance()).getSerializedObjectName();
                                List<ParsedNode> elementNodes = Collections.emptyList();
                                if (childNode.getName().equals(elementName)) {
                                    elementNodes = Collections.singletonList(childNode);
                                } else if (childNode.getName().equals(childNode.getName())) {
                                    elementNodes = childNode.getChildren(null, elementName);
                                }
                                if (!elementNodes.isEmpty()) {
                                    Collection collection = ((Collection) getSerializableFieldValue(childNode.getName()));
                                    for (ParsedNode node : elementNodes) {
                                        LiquibaseSerializable childObject = (LiquibaseSerializable) collectionType.newInstance();
                                        childObject.load(node, resourceAccessor);
                                        collection.add(childObject);
                                    }
                                }
                            }
                        }
                    } if (LiquibaseSerializable.class.isAssignableFrom(dataTypeClass)) {
                        LiquibaseSerializable childObject = (LiquibaseSerializable) dataTypeClass.newInstance();
                        childObject.load(childNode, resourceAccessor);
                        setSerializableFieldValue(childNode.getName(), childObject);
                    } else if (childNode.getValue() != null) {
                        ObjectUtil.setProperty(this, childNode.getName(), childNode.getValue().toString());
                    }
                } else {
                    for (String field : this.getSerializableFields()) {
                        Class dataTypeClass = this.getSerializableFieldDataTypeClass(field);
                        if (Collection.class.isAssignableFrom(dataTypeClass)) {
                            Type[] dataTypeClassParameters = getSerializableFieldDataTypeClassParameters(field);
                            if (dataTypeClassParameters.length == 1) {
                                Class collectionType = null;
                                if (dataTypeClassParameters[0] instanceof Class) {
                                    collectionType = (Class) dataTypeClassParameters[0];
                                } else if (dataTypeClassParameters[0] instanceof ParameterizedType) {
                                    collectionType = (Class) ((ParameterizedType) dataTypeClassParameters[0]).getRawType();
                                }
                                if (collectionType != null
                                        && LiquibaseSerializable.class.isAssignableFrom(collectionType)
                                        && !collectionType.isInterface()
                                        && !Modifier.isAbstract(collectionType.getModifiers())) {

                                    String elementName = ((LiquibaseSerializable) collectionType.newInstance()).getSerializedObjectName();
                                    List<ParsedNode> elementNodes = Collections.emptyList();
                                    if (childNode.getName().equals(elementName)) {
                                        elementNodes = Collections.singletonList(childNode);
                                    } else if (childNode.getName().equals(field)) {
                                        elementNodes = childNode.getChildren(null, elementName);
                                    }
                                    if (!elementNodes.isEmpty()) {
                                        Collection collection = ((Collection) getSerializableFieldValue(field));
                                        for (ParsedNode node : elementNodes) {
                                            LiquibaseSerializable childObject = (LiquibaseSerializable) collectionType.newInstance();
                                            childObject.load(node, resourceAccessor);
                                            collection.add(childObject);
                                        }
                                    }
                                }
                            }
                        }
                    }
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

    protected Class getSerializableFieldDataTypeClass(String field) {
        return ReflectionSerializer.getInstance().getDataTypeClass(this, field);
    }

    protected Type[] getSerializableFieldDataTypeClassParameters(String field) {
        return ReflectionSerializer.getInstance().getDataTypeClassParameters(this, field);
    }

    protected void setSerializableFieldValue(String field, Object value) {
        ReflectionSerializer.getInstance().setValue(this, field, value);
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
