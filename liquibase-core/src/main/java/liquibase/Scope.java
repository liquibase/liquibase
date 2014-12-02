package liquibase;

import liquibase.util.SmartMap;

/**
 * This scope object is used to hold configuration and other parameters within a call without needing complex method signatures.
 * It also allows new parameters to be added by extensions without affecting standard method signatures.
 * Scope objects can be created in a hierarchical manner with the {@link #createChildScope()} method.
 * Values set in parent scopes are visible in child scopes, but values in child scopes are not visible to parent scopes.
 * Values with the same key in different scopes "mask" each other with the value furthest down the scope chain being returned.
 */
public class Scope {

    private Scope parent;
    private SmartMap values = new SmartMap();

    /**
     * Creates a new "root" scope.
     */
    public Scope() {
    }

    protected Scope(Scope parent) {
        this.parent = parent;
    }

    /**
     * Returns the parent scope to this scope. Returns null if this is a root scope.
     */
    public Scope getParent() {
        return parent;
    }

    /**
     * Creates a new scope that is a child of this scope.
     */
    public Scope createChildScope() {
        return new Scope(this);
    }

    /**
     * Return the value associated with the given key in this scope or any parent scope.
     * The value is converted to the given type if necessary using {@link liquibase.util.ObjectUtil#convert(Object, Class)}.
     * Returns null if key is not defined in this or any parent scopes.
     */
    public  <T> T get(String key, Class<T> type) {
        T value = values.get(key, type);
        if (value == null && parent != null) {
            value = parent.get(key, type);
        }
        return value;
    }

    /**
     * Return the value associated with the given key in this scope or any parent scope.
     * If the value is not defined, the passed defaultValue is returned.
     * The value is converted to the given type if necessary using {@link liquibase.util.ObjectUtil#convert(Object, Class)}.
     */
    public  <T> T get(String key, T defaultValue) {
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
     * Sets the given key/value in this scope and returns "this". Replaces any existing key that may already exist.
     * A key that matches a parent scope's key doesn't affect the parent scope but will mask the value for this scope.
     */
    public Scope set(String key, Object value) {
        values.put(key, value);
        return this;
    }

}
