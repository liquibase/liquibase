package liquibase.precondition;

import liquibase.exception.SetupException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;
import liquibase.resource.ResourceAccessor;
import liquibase.util.ObjectUtil;
import liquibase.util.StringUtils;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class AbstractPrecondition implements Precondition {

    private Set<String> serializableFields;

    @Override
    public String getSerializedObjectName() {
        return getName();
    }

    public void load(ParsedNode parsedNode, ResourceAccessor resourceAccessor) throws ParsedNodeException {
        for (ParsedNode childNode : parsedNode.getChildren()) {
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

    @Override
    public ParsedNode serialize() {
        return null;
    }

    @Override
    public Set<String> getSerializableFields() {
        if (serializableFields == null) {
            serializableFields = new HashSet<String>();
            try {
                for (PropertyDescriptor property : Introspector.getBeanInfo(this.getClass()).getPropertyDescriptors()) {
                    Method readMethod = property.getReadMethod();
                    Method writeMethod = property.getWriteMethod();
                    if (readMethod == null) {
                        try {
                            readMethod = this.getClass().getMethod("is"+ StringUtils.upperCaseFirst(property.getName()));
                        } catch (Exception ignore) {
                            //it was worth a try
                        }
                    }
                    if (readMethod != null && writeMethod != null) {
                        serializableFields.add(property.getDisplayName());
                    }

                }
            } catch (IntrospectionException e) {
                throw new UnexpectedLiquibaseException(e);
            }
            serializableFields = Collections.unmodifiableSet(serializableFields);
        }

        return serializableFields;
    }

    @Override
    public Object getSerializableFieldValue(String field) {
        try {
            return ObjectUtil.getProperty(this, field);
        } catch (IllegalAccessException e) {
            throw new UnexpectedLiquibaseException(e);
        } catch (InvocationTargetException e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    @Override
    public SerializationType getSerializableFieldType(String field) {
        return SerializationType.NAMED_FIELD;
    }
}
