package liquibase;

import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.util.ObjectUtil;
import liquibase.util.SmartMap;
import liquibase.util.StringUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Convenience class implementing ExtensibleObject. It is usually easiest to extend this class rather than implement all of ExtensibleObject yourself.
 */
public class AbstractExtensibleObject implements ExtensibleObject {

    /**
     * Additional non-standard attributes.
     */
    private SmartMap attributes = new SmartMap();

    /**
     * Cache of fields on this object. Lazy loaded in {@link #getAttributeFields()}
     */
    private static Map<Class, Map<String, Field>> attributeFieldCache = new HashMap<>();

    public AbstractExtensibleObject() {
    }

    /**
     * Creates a new object with the given attributes.
     */
    public AbstractExtensibleObject(Map<String, ?> values) {
        for (Map.Entry<String, ?> entry : values.entrySet()) {
            this.set(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public SortedSet<String> getAttributes() {
        SortedSet<String> returnSet = new TreeSet<>(attributes.keySet());
        for (String field : getAttributeFields().keySet()) {
            if (has(field)) {
                returnSet.add(field);
            }
        }
        return Collections.unmodifiableSortedSet(returnSet);
    }

    @Override
    public ObjectMetaData getObjectMetaData() {
        ObjectMetaData metaData = new ObjectMetaData();

        for (Field field : getAttributeFields().values()) {
            ObjectMetaData.Attribute attribute = new ObjectMetaData.Attribute(field.getName());
            attribute.type = field.getGenericType();

            ExtensibleObjectAttribute annotation = field.getAnnotation(ExtensibleObjectAttribute.class);
            if (annotation != null) {
                attribute.description = annotation.description();
                attribute.required = annotation.required();
            }

            metaData.attributes.add(attribute);
        }
        return metaData;
    }


    /**
     * Return true if the given key is defined.
     */
    public boolean has(String key) {
        return get(key, Object.class) != null;
    }

    @Override
    public <T> T get(String attribute, Class<T> type) {
        return get(attribute, null, type);
    }

    @Override
    public <T> T get(String attribute, T defaultValue) {
        Class<T> type = (Class<T>) Object.class;
        if (defaultValue != null) {
            type = (Class<T>) defaultValue.getClass();
        }
        return get(attribute, defaultValue, type);
    }

    private Map<String, Field> getAttributeFields() {
        Map<String, Field> fields = attributeFieldCache.get(this.getClass());
        if (fields == null) {
            fields = new HashMap<>();
            for (Field field : this.getClass().getFields()) {
                int modifiers = field.getModifiers();
                if (Modifier.isPublic(modifiers) && !field.isSynthetic() && !Modifier.isFinal(modifiers) && !Modifier.isStatic(modifiers)) {
                    fields.put(field.getName(), field);
                }
            }
            attributeFieldCache.put(this.getClass(), fields);
        }
        return fields;
    }

    protected <T> T get(String attribute, T defaultValue, Class<T> type) {
        Object value;
        if (attribute.contains(".")) {
            List path = getValuePath(attribute, type);
            value = path.get(path.size() - 1);
        } else {
            value = getFieldValue(attribute, type);
        }

        if (value == null) {
            return defaultValue;
        } else {
            return (T) value;
        }
    }

    public List getValuePath(String attributes, Class lastType) {
        List path = new ArrayList();

        String baseField;
        String remainingAttribute = null;
        int separatorIndex = attributes.indexOf('.');
        if (separatorIndex < 0) {
            baseField = attributes;
        } else {
            baseField = attributes.substring(0, separatorIndex);
            remainingAttribute = attributes.substring(separatorIndex + 1);
        }

        Object lastValue = this;

        while (baseField != null) {
            boolean isLastField = remainingAttribute == null;

            Object newValue;
            Class typeToGet = isLastField ? lastType : Object.class;

            if (lastValue == null) {
                newValue = null;
            } else if (lastValue instanceof ExtensibleObject) {
                newValue = ((ExtensibleObject) lastValue).get(baseField, typeToGet);
            } else if (lastValue instanceof Collection) {
                newValue = new ArrayList();
                boolean foundNonNullValue = false;
                for (Object object : (Collection) lastValue) {
                    if (object == null) {
                        ((Collection) newValue).add(null);
                    } else if (object instanceof ExtensibleObject) {
                        ((Collection) newValue).add(((ExtensibleObject) object).get(baseField, typeToGet));
                        foundNonNullValue = true;
                    } else {
                        throw new UnexpectedLiquibaseException("Cannot traverse field(s) " + baseField + " on a " + object.getClass().getName());
                    }
                }
                if (!foundNonNullValue) {
                    newValue = null;
                }
            } else {
                throw new UnexpectedLiquibaseException("Cannot traverse field(s) " + baseField + " on a " + lastValue.getClass().getName());
            }

            if (newValue instanceof Collection) {
                List flattenedCollection = new ArrayList();
                for (Object obj : (Collection) newValue) {
                    if (obj instanceof Collection) {
                        flattenedCollection.addAll((Collection) obj);
                    } else {
                        flattenedCollection.add(obj);
                    }
                }
                newValue = flattenedCollection;
            }

            path.add(newValue);
            lastValue = newValue;


            if (remainingAttribute == null) {
                baseField = null;
            } else {
                separatorIndex = remainingAttribute.indexOf('.');
                if (separatorIndex < 0) {
                    baseField = remainingAttribute;
                    remainingAttribute = null;
                } else {
                    baseField = remainingAttribute.substring(0, separatorIndex);
                    remainingAttribute = remainingAttribute.substring(separatorIndex + 1);
                }
            }
        }


        return path;
    }

    protected Object getFieldValue(String attribute, Class type) {
        Object value;

        Field field = getAttributeFields().get(attribute);
        if (field == null) {
            value = attributes.get(attribute, type);
        } else {
            try {
                value = ObjectUtil.convert(field.get(this), type);
            } catch (IllegalAccessException e) {
                throw new UnexpectedLiquibaseException(e);
            }
        }
        return value;
    }

    @Override
    public ExtensibleObject set(String attribute, Object value) {
        Field field = getAttributeFields().get(attribute);
        if (field == null) {
            attributes.set(attribute, value);
        } else {
            try {
                field.set(this, ObjectUtil.convert(value, field.getType()));
            } catch (Exception e) {
                throw new UnexpectedLiquibaseException("Error setting " + getClass().getName() + "." + attribute, e);
            }
        }

        return this;
    }

    public String describe() {
        String name = getClass().getSimpleName();
        return name + "{" + StringUtil.join(this, ", ", new StringUtil.DefaultFormatter()) + "}";
    }

    @Override
    public String toString() {
        return describe();
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    /**
     * Default implementation counts objects equal if their describe() methods return equal strings.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        return obj instanceof AbstractExtensibleObject && this.describe().equals(((AbstractExtensibleObject) obj).describe());
    }

    @Override
    public Object clone() {
        try {
            AbstractExtensibleObject clone = (AbstractExtensibleObject) super.clone();
            for (String attr : getAttributes()) {
                Object value = this.get(attr, Object.class);
                if (value instanceof Collection) {
                    try {
                        Collection valueClone = (Collection) value.getClass().newInstance();
                        for (Object obj : ((Collection) value)) {
                            valueClone.add(obj);
                        }
                        value = valueClone;
                    } catch (Exception e) {
                        //keep original object
                    }
                } else if (value instanceof Map) {
                    try {
                        Map valueClone = (Map) value.getClass().newInstance();
                        for (Map.Entry obj : ((Map<?, ?>) value).entrySet()) {
                            valueClone.put(obj.getKey(), obj.getValue());
                        }
                        value = valueClone;
                    } catch (Exception e) {
                        //keep original object
                    }
                } else if (value instanceof AbstractExtensibleObject) {
                    value = ((AbstractExtensibleObject) value).clone();
                }
                clone.set(attr, value);
            }

            return clone;
        } catch (CloneNotSupportedException e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }
}
