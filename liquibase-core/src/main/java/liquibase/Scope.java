package liquibase;

import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.OfflineConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.listener.LiquibaseListener;
import liquibase.logging.LogService;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;
import liquibase.servicelocator.ServiceLocator;
import liquibase.servicelocator.StandardServiceLocator;
import liquibase.util.SmartMap;

import java.lang.reflect.Constructor;
import java.util.*;

/**
 * This scope object is used to hold configuration and other parameters within a call without needing complex method signatures.
 * It also allows new parameters to be added by extensions without affecting standard method signatures.
 * Scope objects can be created in a hierarchical manner with the {@link #child(Map, ScopedRunner)} or {@link #child(String, Object, ScopedRunner)} methods.
 * Values set in parent scopes are visible in child scopes, but values in child scopes are not visible to parent scopes.
 * Values with the same key in different scopes "mask" each other with the value furthest down the scope chain being returned.
 */
public class Scope {

    /**
     * Enumeration containing standard attributes. Normally use methods like convenience {@link #getResourceAccessor()} or {@link #getDatabase()}
     */
    public enum Attr {
        resourceAccessor,
        classLoader,
        database,
        quotingStrategy,
        changeLogHistoryService,
        lockService,
        executeMode,
        lineSeparator, serviceLocator,
    }

    private static ScopeManager scopeManager;

    private Scope parent;
    private SmartMap values = new SmartMap();

    private LiquibaseListener listener;

    public static Scope getCurrentScope() {
        if (scopeManager == null) {
            scopeManager = new SingletonScopeManager();
            scopeManager.setCurrentScope(new Scope());
        }
        return scopeManager.getCurrentScope();
    }

    public static void setScopeManager(ScopeManager scopeManager)  {
        Scope currentScope = getCurrentScope();
        if (currentScope == null) {
            currentScope = new Scope();
        }

        try {
            currentScope = scopeManager.init(currentScope);
        } catch (Exception e) {
            LogService.getLog(Scope.class).warning(e.getMessage(), e);
        }
        scopeManager.setCurrentScope(currentScope);

        Scope.scopeManager = scopeManager;


    }

    /**
     * Creates a new "root" scope.
     * Defaults resourceAccessor to {@link ClassLoaderResourceAccessor}.
     * Defaults serviceLocator to {@link StandardServiceLocator}
     */
    private Scope() {
        values.put(Attr.resourceAccessor.name(), new ClassLoaderResourceAccessor());
        values.put(Attr.serviceLocator.name(), new StandardServiceLocator());

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
    public static void child(Map<String, Object> scopeValues, ScopedRunner runner) throws Exception {
        child((LiquibaseListener) null, scopeValues, runner);
    }

    /**
     * Creates a new child scope that includes the given {@link LiquibaseListener}.
     * You cannot unassign a listener, they simply fall out of scope when the Scope does.
     *
     * @see #getListeners(Class)
     */
    public static void child(LiquibaseListener listener, ScopedRunner runner) throws Exception {
        child(listener, null, runner);
    }

    public static void child(LiquibaseListener listener, Map<String, Object> scopeValues, ScopedRunner runner) throws Exception{
        Scope originalScope = getCurrentScope();
        Scope child = new Scope(originalScope, scopeValues);
        child.listener = listener;
        try {
            scopeManager.setCurrentScope(child);
            runner.run();
        } finally {
            scopeManager.setCurrentScope(originalScope);
        }
    }

    /**
     * Creates a new scope that is a child of this scope.
     */
    public void child(String newValueKey, Object newValue, ScopedRunner runner) throws Exception {
        Map<String, Object> scopeValues = new HashMap<String, Object>();
        scopeValues.put(newValueKey, newValue);

        child(scopeValues, runner);
    }

    public void child(Enum newValueKey, Object newValue, ScopedRunner runner) throws Exception {
        child(newValueKey.name(), newValue, runner);
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


    public <T> T get(Enum key, Class<T> type) {
        return get(key.name(), type);
    }

    public <T> T get(Enum key, T defaultValue) {
        return get(key.name(), defaultValue);
    }

    /**
     * Return the value associated with the given key in this scope or any parent scope.
     * The value is converted to the given type if necessary using {@link liquibase.util.ObjectUtil#convert(Object, Class)}.
     * Returns null if key is not defined in this or any parent scopes.
     */
    public <T> T get(String key, Class<T> type) {
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
     * Looks up the singleton object of the given type. If the singleton has not been created yet, it will be instantiated.
     * The singleton is a singleton based on the root scope and the same object will be returned for all child scopes of the root.
     */
    public <T extends SingletonObject> T getSingleton(Class<T> type) {
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

    public Database getDatabase() {
        return get(Attr.database, Database.class);
    }

    public ClassLoader getClassLoader() {
        return get(Attr.classLoader, ClassLoader.class);
    }

    public ClassLoader getClassLoader(boolean fallbackToContextClassLoader) {
        ClassLoader classLoader = getClassLoader();
        if (classLoader == null && fallbackToContextClassLoader) {
            return Thread.currentThread().getContextClassLoader();
        }
        return classLoader;
    }

    public ServiceLocator getServiceLocator() {
        return get(Attr.serviceLocator, ServiceLocator.class);
    }

    public ResourceAccessor getResourceAccessor() {
        return get(Attr.resourceAccessor, ResourceAccessor.class);
    }

    public String getLineSeparator() {
        return get(Attr.lineSeparator, System.lineSeparator());
    }


    /**
     * Returns {@link LiquibaseListener}s defined in this scope and/or all its parents that are of the given type.
     */
    public <T extends LiquibaseListener> Collection<T> getListeners(Class<T> type) {
        List<T> returnList = new ArrayList<>();

        Scope scopeToCheck = this;
        while (scopeToCheck != null) {
            if (scopeToCheck.listener != null && type.isAssignableFrom(scopeToCheck.listener.getClass())) {
                returnList.add((T) scopeToCheck.listener);
            }
            scopeToCheck = scopeToCheck.getParent();
        }

        return returnList;
    }

    @Override
    public String toString() {
        return describe();
    }

    public String describe() {
        String databaseName = null;
        Database database = getDatabase();
        if (database != null) {
            databaseName = database.getShortName();

            DatabaseConnection connection = database.getConnection();
            if (connection == null) {
                databaseName = "unconnected " + databaseName;
            } else if (connection instanceof OfflineConnection) {
                databaseName = "offline " + databaseName;
            } else if (connection instanceof JdbcConnection) {
                databaseName = "jdbc " + databaseName;
            }
        }
        return "scope(database=" + databaseName + ")";
    }

    public interface ScopedRunner {
        void run() throws Exception;
    }

}
