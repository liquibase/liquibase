package liquibase;

import liquibase.util.SmartMap;

import java.util.*;

/**
 * Convenience class implementing ExtensibleObject. It is usually easiest to extend this class rather than implement all of ExtensibleObject yourself.
 */
public class AbstractExtensibleObject implements ExtensibleObject {

    private SmartMap attributes = new SmartMap();
    private Set<String> standardAttributeNames;

    @Override
    public Set<String> getAttributeNames() {
        return attributes.keySet();
    }

    /**
     * Default implementation looks for an inner enum called "Attr" and returns the fields in there
     */
    @Override
    public Set<String> getStandardAttributeNames() {
        if (standardAttributeNames == null) {
            standardAttributeNames = new HashSet<>();
            for (Class declaredClass : this.getClass().getDeclaredClasses()) {
                if (declaredClass.getSimpleName().equals("Attr") && Enum.class.isAssignableFrom(declaredClass)) {
                    for (Object obj : declaredClass.getEnumConstants()) {
                        standardAttributeNames.add(((Enum) obj).name());
                    }
                    break;
                }
            }
        }
        return standardAttributeNames;
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
        return attributes.get(attribute, type);
    }

    @Override
    public <T> T get(String attribute, T defaultValue) {
        return attributes.get(attribute, defaultValue);
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
        attributes.set(attribute, value);

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
