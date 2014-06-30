package liquibase;

import liquibase.util.ObjectUtil;

import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Convenience class implementing ExtensibleObject. It is usually easiest to extend this class rather than implement all of ExtensibleObject yourself.
 */
public class AbstractExtensibleObject implements ExtensibleObject {

    private SortedMap<String, Object> attributes = new TreeMap<String, Object>();

    @Override
    public Set<String> getAttributes() {
        return attributes.keySet();
    }

    @Override
    public <T> T getAttribute(String attribute, Class<T> type) {
        return (T) ObjectUtil.convert(attributes.get(attribute), type);
    }

    @Override
    public <T> T getAttribute(String attribute, T defaultValue) {
        Class type;
        if (defaultValue == null) {
            type = Object.class;
        } else {
            type = defaultValue.getClass();
        }
        Object value = getAttribute(attribute, type);

        if (value == null) {
            return defaultValue;
        }
        return (T) value;
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
}
