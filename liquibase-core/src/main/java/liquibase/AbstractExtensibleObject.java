package liquibase;

import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.serializer.LiquibaseSerializable;
import liquibase.structure.ObjectReference;
import liquibase.util.ObjectUtil;
import liquibase.util.SmartMap;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Convenience class implementing ExtensibleObject. It is usually easiest to extend this class rather than implement all of ExtensibleObject yourself.
 */
public class AbstractExtensibleObject implements ExtensibleObject {

    private SmartMap attributes = new SmartMap();
    private Set<String> standardAttributeNames;

    private static Map<Class, Map<String, Field>> attributeFieldCache = new HashMap<>();

    @Override
    public Set<String> getAttributeNames() {
        HashSet<String> returnSet = new HashSet<>(attributes.keySet());
        for (String field : getAttributeFields().keySet()) {
            if (has(field)) {
                returnSet.add(field);
            }
        }
        return Collections.unmodifiableSet(returnSet);
    }

    /**
     * Default implementation looks for an inner enum called "Attr" and returns the fields in there
     */
    @Override
    public Set<String> getStandardAttributeNames() {
        return getAttributeFields().keySet();
    }

    /**
     * Return true if the given key is defined.
     */
    public boolean has(String key) {
        return get(key, Object.class) != null;
    }

    /**
     * Return true if the given key is defined.
     */
    public boolean has(Enum key) {
        return has(key.name());
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
                if (Modifier.isPublic(modifiers) && !field.isSynthetic()) {
                    fields.put(field.getName(), field);
                }
            }
            attributeFieldCache.put(this.getClass(), fields);
        }
        return fields;
    }

    private <T> T get(String attribute, T defaultValue, Class<T> type) {
        T value;

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
        if (value == null) {
            return defaultValue;
        }
        return value;
    }


    @Override
    public <T> T get(Enum attribute, Class<T> type) {
        return get(attribute.name(), type);
    }

    @Override
    public <T> T get(Enum attribute, T defaultValue) {
        return get(attribute.name(), defaultValue);
    }

    @Override
    public ExtensibleObject set(Enum attribute, Object value) {
        return this.set(attribute.name(), value);
    }

    @Override
    public ExtensibleObject set(String attribute, Object value) {
        Field field = getAttributeFields().get(attribute);
        if (field == null) {
            attributes.set(attribute, value);
        } else {
            try {
                field.set(this, ObjectUtil.convert(value, field.getType()));
            } catch (IllegalAccessException e) {
                throw new UnexpectedLiquibaseException(e);
            }
        }

        return this;
    }

    @Override
    public ExtensibleObject add(String attribute, Object value) {
        Object existingValue = get(attribute, Object.class);
        if (existingValue == null) {
            existingValue = new ArrayList<>();
            set(attribute, existingValue);
        } else if (!(existingValue instanceof Collection)) {
            List newCollection = new ArrayList();
            newCollection.add(existingValue);
            set(attribute, newCollection);
            existingValue = newCollection;
        }

        ((Collection) existingValue).add(value);

        return this;
    }

    @Override
    public ExtensibleObject add(Enum attribute, Object value) {
        return add(attribute.name(), value);
    }
}
