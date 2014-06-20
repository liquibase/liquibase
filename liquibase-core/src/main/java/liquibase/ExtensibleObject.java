package liquibase;

import java.util.Set;
import java.util.SortedMap;

public interface ExtensibleObject {
    Set<String> getAttributes();

    <T> T getAttribute(String attribute, Class<T> type);

    <T> T getAttribute(String attribute, T defaultValue);

    Object setAttribute(String attribute, Object value);

    public SortedMap<String, Object> getAttributeMap();
}
