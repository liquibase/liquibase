package liquibase;

import java.util.*;

public abstract class AbstractExtensibleObject implements ExtensibleObject {

    private SortedMap<String, Object> attributes = new TreeMap<String, Object>();

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
    public Object setAttribute(String attribute, Object value) {
        if (value == null) {
            attributes.remove(attribute);
        } else {
            attributes.put(attribute, value);
        }

        return this;
    }

    protected SortedMap<String, Object> getAttributeMap() {
        return attributes;
    }


}
