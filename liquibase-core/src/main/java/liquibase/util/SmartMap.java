package liquibase.util;

import java.util.*;

/**
 * Implementation of Map with the added methods {@link #get(String, Class)}  and {@link #get(String, Object)} to make the return values type safe and/or auto-converted.
 * Also adds {@link #set(String, Object)} for easier builder-style code.
 * Returns keys in alphabetical order.
 */
public class SmartMap implements Map<String, Object> {
    private SortedMap<String, Object> values = new TreeMap<String, Object>();

    @Override
    public int size() {
        return values.size();
    }

    @Override
    public boolean isEmpty() {
        return values.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return values.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return values.containsValue(value);
    }

    @Override
    public Object get(Object key) {
        return values.get(key);
    }


    /**
     * Return the value for the given key converted to the passed type. If the value associated with the key is null, null is returned.
     * If the stored value is different than the passed type, a conversion is attempted using {@link ObjectUtil#convert(Object, Class)}.
     * Any conversion is done only on the return value. The stored value is unchanged.
     */
    public <T> T get(String key, Class<T> type) {
        return (T) ObjectUtil.convert(values.get(key), type);
    }

    /**
     * Return the value for the given key converted to the type of the default value.
     * If the value is null, defaultValue is returned.
     * Conversion is done using {@link #get(String, Class)}
     */
    public <T> T get(String key, T defaultValue) {
        Class type;
        if (defaultValue == null) {
            type = Object.class;
        } else {
            type = defaultValue.getClass();
        }
        Object value = get(key, type);

        if (value == null) {
            return defaultValue;
        }
        return (T) value;
    }


    /**
     * Like normal {@link Map#put(Object, Object)} operation, but if null is passed as "value" it removes the key/value from the map.
     */
    @Override
    public Object put(String key, Object value) {
        if (value == null) {
            return values.remove(key);
        } else {
            return values.put(key, value);
        }
    }

    /**
     * Works like {@link #put(String, Object)} but returns this SmartMap rather than the old value.
     */
    public SmartMap set(String key, Object value) {
        put(key, value);
        return this;
    }

    @Override
    public Object remove(Object key) {
        return values.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ?> m) {
        values.putAll(m);
    }

    @Override
    public void clear() {
        values.clear();
    }

    @Override
    public boolean equals(Object o) {
        return values.equals(o);
    }

    @Override
    public int hashCode() {
        return values.hashCode();
    }

    @Override
    public Set<String> keySet() {
        return values.keySet();
    }

    @Override
    public Collection<Object> values() {
        return values.values();
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return values.entrySet();
    }
}
