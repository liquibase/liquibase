package liquibase;

import liquibase.util.SmartMap;

import java.util.Set;

/**
 * Convenience class implementing ExtensibleObject. It is usually easiest to extend this class rather than implement all of ExtensibleObject yourself.
 */
public class AbstractExtensibleObject implements ExtensibleObject {

    private SmartMap attributes = new SmartMap();

    @Override
    public Set<String> getAttributeNames() {
        return attributes.keySet();
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
}
