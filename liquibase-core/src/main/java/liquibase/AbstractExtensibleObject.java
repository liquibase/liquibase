package liquibase;

import liquibase.util.SmartMap;

import java.util.Set;

/**
 * Convenience class implementing ExtensibleObject. It is usually easiest to extend this class rather than implement all of ExtensibleObject yourself.
 */
public class AbstractExtensibleObject implements ExtensibleObject {

    private SmartMap attributes = new SmartMap();

    @Override
    public Set<String> getAttributes() {
        return attributes.keySet();
    }

    @Override
    public <T> T getAttribute(String attribute, Class<T> type) {
        return attributes.get(attribute, type);
    }

    @Override
    public <T> T getAttribute(String attribute, T defaultValue) {
        return attributes.get(attribute, defaultValue);
    }

    @Override
    public <T> T getAttribute(Enum attribute, Class<T> type) {
        return getAttribute(attribute.name(), type);
    }

    @Override
    public <T> T getAttribute(Enum attribute, T defaultValue) {
        return getAttribute(attribute.name(), defaultValue);
    }

    @Override
    public Object setAttribute(Enum attribute, Object value) {
        return this.setAttribute(attribute.name(), value);
    }

    @Override
    public Object setAttribute(String attribute, Object value) {
        attributes.set(attribute, value);

        return this;
    }
}
