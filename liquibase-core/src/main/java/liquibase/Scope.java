package liquibase;

import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.resource.ResourceAccessor;
import liquibase.util.SmartMap;
import liquibase.util.Validate;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * This scope object is used to hold configuration and other parameters within a call without needing complex method signatures.
 * It also allows new parameters to be added by extensions without affecting standard method signatures.
 * Scope objects can be created in a hierarchical manner with the {@link #child(java.util.Map)} or {@link #child(String, Object)} methods.
 * Values set in parent scopes are visible in child scopes, but values in child scopes are not visible to parent scopes.
 * Values with the same key in different scopes "mask" each other with the value furthest down the scope chain being returned.
 */
public class Scope {

    public static enum Attr {
        resourceAccessor,
        database,
    }

    private Scope parent;
    private SmartMap values = new SmartMap();

    /**
     * Creates a new "root" scope.
     */
    public Scope(ResourceAccessor resourceAccessor, Map<String, Object> scopeValues) {
        this((Scope) null, scopeValues);
        Validate.notNull(resourceAccessor, "ResourceAccessor not set");
        this.values.put(Attr.resourceAccessor.name(), resourceAccessor);
    }

    protected Scope(Scope parent, Map<String, Object> scopeValues) {
        this.parent = parent;
        if (scopeValues != null) {
            for (Map.Entry<String, Object> entry : scopeValues.entrySet()) {
                values.put(entry.getKey(), entry.getValue());
            }
        }
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
    public Scope child(Map<String, Object> scopeValues) {
        return new Scope(this, scopeValues);
    }

    /**
     * Creates a new scope that is a child of this scope.
     */
    public Scope child(String newValueKey, Object newValue) {
        Map<String, Object> scopeValues = new HashMap<String, Object>();
        scopeValues.put(newValueKey, newValue);

        return new Scope(this, scopeValues);
    }

    public Scope child(Enum newValueKey, Object newValue) {
        return child(newValueKey.name(), newValue);
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


    public  <T> T get(Enum key, Class<T> type) {
        return get(key.name(), type);
    }

    public  <T> T get(Enum key, T defaultValue) {
        return get(key.name(), defaultValue);
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
     * Looks up the singleton object of the given type. If the singleton has not been created yet, it will be instantiated.
     * The singleton is a singleton based on the root scope and the same object will be returned for all child scopes of the root.
     */
    public <T> T getSingleton(Class<T> type) {
        if (getParent() != null) {
            return getParent().getSingleton(type);
        }

        String key = type.getName();
        T singleton = get(key, type);
        if (singleton == null) {
            try {
                try {
                    Constructor<T> constructor = type.getDeclaredConstructor(Scope.class);
                    constructor.setAccessible(true);
                    singleton = constructor.newInstance(this);
                } catch (NoSuchMethodException e) { //try without scope
                    Constructor<T> constructor = type.getDeclaredConstructor();
                    constructor.setAccessible(true);
                    singleton = constructor.newInstance();
                }
            } catch (Exception e) {
                throw new UnexpectedLiquibaseException(e);
            }

            values.put(key, singleton);
        }
        return singleton;
    }
}
