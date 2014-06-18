package liquibase;

import java.util.Set;

public interface ExtensibleObject {
    Set<String> getAttributes();

    <T> T getAttribute(String attribute, Class<T> type);

    <T> T getAttribute(String attribute, T defaultValue);

    Object setAttribute(String attribute, Object value);
}
