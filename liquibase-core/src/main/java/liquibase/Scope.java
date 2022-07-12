package liquibase;

import liquibase.configuration.LiquibaseConfiguration;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.OfflineConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.listener.LiquibaseListener;
import liquibase.logging.LogService;
import liquibase.logging.Logger;
import liquibase.logging.core.JavaLogService;
import liquibase.logging.core.LogServiceFactory;
import liquibase.osgi.Activator;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;
import liquibase.servicelocator.ServiceLocator;
import liquibase.servicelocator.StandardServiceLocator;
import liquibase.ui.ConsoleUIService;
import liquibase.ui.UIService;
import liquibase.util.SmartMap;
import liquibase.util.StringUtil;

import java.lang.reflect.Constructor;
import java.nio.charset.Charset;
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
        logService,
        ui,
        resourceAccessor,
        classLoader,
        database,
        quotingStrategy,
        changeLogHistoryService,
        lockService,
        executeMode,
        lineSeparator,
        serviceLocator,

        /**
         * @deprecated use {@link GlobalConfiguration#FILE_ENCODING}
         */
        fileEncoding,
        databaseChangeLog,
        changeSet,
        osgiPlatform
    }

    private static ScopeManager scopeManager;

    private Scope parent;
    private SmartMap values = new SmartMap();
    private String scopeId;

    private LiquibaseListener listener;

    public static Scope getCurrentScope() {
        if (scopeManager == null) {
            scopeManager = new SingletonScopeManager();
        }
        if (scopeManager.getCurrentScope() == null) {
            Scope rootScope = new Scope();
            scopeManager.setCurrentScope(rootScope);

            rootScope.values.put(Attr.logService.name(), new JavaLogService());
            rootScope.values.put(Attr.resourceAccessor.name(), new ClassLoaderResourceAccessor());
            rootScope.values.put(Attr.serviceLocator.name(), new StandardServiceLocator());

            rootScope.values.put(Attr.ui.name(), new ConsoleUIService());
            rootScope.getSingleton(LiquibaseConfiguration.class).init(rootScope);

            LogService overrideLogService = rootScope.getSingleton(LogServiceFactory.class).getDefaultLogService();
            if (overrideLogService == null) {
                throw new UnexpectedLiquibaseException("Cannot find default log service");
            }
            rootScope.values.put(Attr.logService.name(), overrideLogService);

            //check for higher-priority serviceLocator
            ServiceLocator serviceLocator = rootScope.getServiceLocator();
            for (ServiceLocator possibleLocator : serviceLocator.findInstances(ServiceLocator.class)) {
                if (possibleLocator.getPriority() > serviceLocator.getPriority()) {
                    serviceLocator = possibleLocator;
                }
            }

            rootScope.values.put(Attr.serviceLocator.name(), serviceLocator);
            rootScope.values.put(Attr.osgiPlatform.name(), Activator.OSGIContainerChecker.isOsgiPlatform());
        }
        return scopeManager.getCurrentScope();
    }

    public static void setScopeManager(ScopeManager scopeManager) {
        Scope currentScope = getCurrentScope();
        if (currentScope == null) {
            currentScope = new Scope();
        }

        try {
            currentScope = scopeManager.init(currentScope);
        } catch (Exception e) {
            Scope.getCurrentScope().getLog(Scope.class).warning(e.getMessage(), e);
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
    }

    /**
     * @param parent      The new Scopes parent in the hierarchy of Scopes, not null.
     * @param scopeValues The values for the new Scope.
     */
    protected Scope(Scope parent, Map<String, Object> scopeValues) {
        if (parent == null) {
            throw new UnexpectedLiquibaseException("Cannot pass a null parent to a new Scope. Use Scope.child to correctly create a nested scope");
        }
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
     * Creates a new scope that is a child of this scope.
     */
    public static <ReturnType> ReturnType child(Map<String, Object> scopeValues, ScopedRunnerWithReturn<ReturnType> runner) throws Exception {
        return child(null, scopeValues, runner);
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

    public static void child(LiquibaseListener listener, Map<String, Object> scopeValues, ScopedRunner runner) throws Exception {
        child(listener, scopeValues, () -> {
            runner.run();
            return null;
        });
    }

    public static <T> T child(LiquibaseListener listener, Map<String, Object> scopeValues, ScopedRunnerWithReturn<T> runner) throws Exception {
        String scopeId = enter(listener, scopeValues);

        try {
            return runner.run();
        } finally {
            exit(scopeId);
        }
    }

    /**
     * Convenience version of {@link #enter(LiquibaseListener, Map)} with no {@link LiquibaseListener}
     */
    public static String enter(Map<String, Object> scopeValues) throws Exception {
        return enter(null, scopeValues);
    }

    /**
     * Creates a new scope without passing a ScopedRunner.
     * This mainly exists for tests where you have a setup/cleanup method pattern.
     * The recommended way to create Scopes is the "child" methods.
     * When done with the scope, call {@link #exit(String)}
     *
     * @return Returns the scopeId to pass to to {@link #exit(String)}
     */
    public static String enter(LiquibaseListener listener, Map<String, Object> scopeValues) throws Exception {
        String scopeId = StringUtil.randomIdentifer(10).toLowerCase();

        Scope originalScope = getCurrentScope();
        Scope child = new Scope(originalScope, scopeValues);
        child.listener = listener;
        child.scopeId = scopeId;
        scopeManager.setCurrentScope(child);

        return scopeId;
    }

    /**
     * Exits the scope started with {@link #enter(LiquibaseListener, Map)}
     *
     * @param scopeId The id of the scope to exit. Throws an exception if the name does not match the current scope.
     */
    public static void exit(String scopeId) throws Exception {
        Scope currentScope = getCurrentScope();
        if (!currentScope.scopeId.equals(scopeId)) {
            throw new RuntimeException("Cannot end scope " + scopeId + " when currently at scope " + currentScope.scopeId);
        }

        scopeManager.setCurrentScope(currentScope.getParent());
    }

    /**
     * Creates a new scope that is a child of this scope.
     */
    public static void child(String newValueKey, Object newValue, ScopedRunner runner) throws Exception {
        Map<String, Object> scopeValues = new HashMap<String, Object>();
        scopeValues.put(newValueKey, newValue);

        child(scopeValues, runner);
    }

    public static void child(Enum newValueKey, Object newValue, ScopedRunner runner) throws Exception {
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

    public Logger getLog(Class clazz) {
        return get(Attr.logService, LogService.class).getLog(clazz);
    }

    public UIService getUI() {
        return get(Attr.ui, UIService.class);
    }

    public Database getDatabase() {
        return get(Attr.database, Database.class);
    }

    public ClassLoader getClassLoader() {
        return get(Attr.classLoader, Thread.currentThread().getContextClassLoader());
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
     * @deprecated use {@link GlobalConfiguration#FILE_ENCODING}
     */
    public Charset getFileEncoding() {
        return get(Attr.fileEncoding, Charset.defaultCharset());
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

    public interface ScopedRunner<T> {
        void run() throws Exception;
    }

    public interface ScopedRunnerWithReturn<T> {
        T run() throws Exception;
    }
}
