package liquibase;

import liquibase.analytics.Event;
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
import liquibase.logging.mdc.CustomMdcObject;
import liquibase.logging.mdc.MdcManager;
import liquibase.logging.mdc.MdcManagerFactory;
import liquibase.logging.mdc.MdcObject;
import liquibase.osgi.ContainerChecker;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;
import liquibase.servicelocator.ServiceLocator;
import liquibase.servicelocator.StandardServiceLocator;
import liquibase.ui.ConsoleUIService;
import liquibase.ui.UIService;
import liquibase.util.CollectionUtil;
import liquibase.util.SmartMap;
import liquibase.util.StringUtil;
import lombok.Getter;

import java.lang.reflect.Constructor;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This scope object is used to hold configuration and other parameters within a call without needing complex method signatures.
 * It also allows new parameters to be added by extensions without affecting standard method signatures.
 * Scope objects can be created in a hierarchical manner with the {@link #child(Map, ScopedRunner)} or {@link #child(String, Object, ScopedRunner)} methods.
 * Values set in parent scopes are visible in child scopes, but values in child scopes are not visible to parent scopes.
 * Values with the same key in different scopes "mask" each other with the value furthest down the scope chain being returned.
 */
public class Scope {

    public static final String CHECKS_MESSAGE =
            "The Liquibase Checks Extension 2.0.0 or higher is required to execute checks commands. " +
                    "Visit https://docs.liquibase.com/pro-extensions to acquire the Checks Extension.";

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
        deploymentId,

        /**
         * @deprecated use {@link GlobalConfiguration#FILE_ENCODING}
         */
        @Deprecated
        fileEncoding,
        databaseChangeLog,
        changeSet,
        osgiPlatform,
        checksumVersion,
        latestChecksumVersion,
        /**
         * A <code>Map<String, String></code> of arguments/configuration properties used in the maven invocation of Liquibase.
         */
        mavenConfigurationProperties,
        analyticsEvent,
        integrationDetails
    }

    public static final String JAVA_PROPERTIES = "javaProperties";

    private static final ThreadLocal<ScopeManager> scopeManager = new ThreadLocal<>();

    private final Scope parent;
    private final SmartMap values = new SmartMap();
    @Getter
    private final String scopeId;
    private static final Map<String, List<MdcObject>> addedMdcEntries = new ConcurrentHashMap<>();

    private LiquibaseListener listener;

    public static Scope getCurrentScope() {
        if (scopeManager.get() == null) {
            scopeManager.set(new SingletonScopeManager());
        }
        if (scopeManager.get().getCurrentScope() == null) {
            Scope rootScope = new Scope();
            scopeManager.get().setCurrentScope(rootScope);

            rootScope.values.put(Attr.logService.name(), new JavaLogService());
            rootScope.values.put(Attr.serviceLocator.name(), new StandardServiceLocator());
            rootScope.values.put(Attr.resourceAccessor.name(), new ClassLoaderResourceAccessor());
            rootScope.values.put(Attr.latestChecksumVersion.name(), ChecksumVersion.V9);
            rootScope.values.put(Attr.checksumVersion.name(), ChecksumVersion.latest());

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
            rootScope.values.put(Attr.osgiPlatform.name(), ContainerChecker.isOsgiPlatform());
            rootScope.values.put(Attr.deploymentId.name(), generateDeploymentId());
        }
        return scopeManager.get().getCurrentScope();
    }

    public static void setScopeManager(ScopeManager scopeManager) {
        Scope.scopeManager.set(scopeManager);
    }

    /**
     * Creates a new "root" scope.
     * Defaults resourceAccessor to {@link ClassLoaderResourceAccessor}.
     * Defaults serviceLocator to {@link StandardServiceLocator}
     */
    private Scope() {
        scopeId = "root";
        parent = null;
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
        scopeId = generateScopeId();
        if (scopeValues != null) {
            values.putAll(scopeValues);
        }
    }

    private String generateScopeId() {
        return StringUtil.randomIdentifier(10).toLowerCase();
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
        Scope originalScope = getCurrentScope();
        Scope child = new Scope(originalScope, scopeValues);
        child.listener = listener;
        scopeManager.get().setCurrentScope(child);

        return child.scopeId;
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

        // clear the MDC values added in this scope
        List<MdcObject> mdcObjects = addedMdcEntries.remove(currentScope.scopeId);
        for (MdcObject mdcObject : CollectionUtil.createIfNull(mdcObjects)) {
            mdcObject.close();
        }

        scopeManager.get().setCurrentScope(currentScope.getParent());
    }

    /**
     * Creates a new scope that is a child of this scope.
     */
    public static void child(String newValueKey, Object newValue, ScopedRunner runner) throws Exception {
        Map<String, Object> scopeValues = new HashMap<>();
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


    public synchronized <T> T get(Enum key, Class<T> type) {
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
        if (value == null && values.containsKey(JAVA_PROPERTIES)) {
            Map javaProperties = values.get(JAVA_PROPERTIES, Map.class);
            value = (T)javaProperties.get(key);
        }
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
    public synchronized <T> T get(String key, T defaultValue) {
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
    public synchronized <T extends SingletonObject> T getSingleton(Class<T> type) {
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

    public String getDeploymentId() { return get(Attr.deploymentId, String.class); }

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

    public ChecksumVersion getChecksumVersion() {
        return get(Attr.checksumVersion, ChecksumVersion.class);
    }

    public String getLineSeparator() {
        return get(Attr.lineSeparator, System.lineSeparator());
    }

    /**
     * @deprecated use {@link GlobalConfiguration#FILE_ENCODING}
     */
    @Deprecated
    public Charset getFileEncoding() {
        return get(Attr.fileEncoding, Charset.defaultCharset());
    }

    /**
     * Get the current MDC manager.
     */
    public MdcManager getMdcManager() {
        MdcManagerFactory mdcManagerFactory = getSingleton(MdcManagerFactory.class);
        return mdcManagerFactory.getMdcManager();
    }

    /**
     * Add a key value pair to the MDC using the MDC manager. This key value pair will be automatically removed from the
     * MDC when this scope exits.
     */
    public MdcObject addMdcValue(String key, String value) {
        return addMdcValue(key, value, true);
    }

    /**
     * Add a key value pair to the MDC using the MDC manager.
     * @param removeWhenScopeExits if true, this key value pair will be automatically removed from the MDC when this
     *                             scope exits. If there is not a demonstrable reason for setting this parameter to false
     *                             then it should be set to true.
     */
    public MdcObject addMdcValue(String key, String value, boolean removeWhenScopeExits) {
        MdcObject mdcObject = getMdcManager().put(key, value, removeWhenScopeExits);
        removeMdcObjectWhenScopeExits(removeWhenScopeExits, mdcObject);

        return mdcObject;
    }

    private void removeMdcObjectWhenScopeExits(boolean removeWhenScopeExits, MdcObject mdcObject) {
        if (removeWhenScopeExits) {
            Scope currentScope = getCurrentScope();
            addedMdcEntries
                    .computeIfAbsent(currentScope.scopeId, k -> new ArrayList<>())
                    .add(mdcObject);
        }
    }


    /**
     * Add a key value pair to the MDC using the MDC manager. This key value pair will be automatically removed from the
     * MDC when this scope exits.
     */
    public MdcObject addMdcValue(String key, Map<String, Object> value) {
        return addMdcValue(key, value, true);
    }

    /**
     * Add a key value pair to the MDC using the MDC manager.
     * @param removeWhenScopeExits if true, this key value pair will be automatically removed from the MDC when this
     *                             scope exits. If there is not a demonstrable reason for setting this parameter to false
     *                             then it should be set to true.
     */
    public MdcObject addMdcValue(String key, Map<String, Object> value, boolean removeWhenScopeExits) {
        MdcObject mdcObject = getMdcManager().put(key, value, removeWhenScopeExits);
        removeMdcObjectWhenScopeExits(removeWhenScopeExits, mdcObject);

        return mdcObject;
    }

    /**
     * Add a key value pair to the MDC using the MDC manager. This key value pair will be automatically removed from the
     * MDC when this scope exits.
     */
    public MdcObject addMdcValue(String key, CustomMdcObject customMdcObject) {
        return addMdcValue(key, customMdcObject, true);
    }

    /**
     * Add a key value pair to the MDC using the MDC manager.
     * @param removeWhenScopeExits if true, this key value pair will be automatically removed from the MDC when this
     *                             scope exits. If there is not a demonstrable reason for setting this parameter to false
     *                             then it should be set to true.
     */
    public MdcObject addMdcValue(String key, CustomMdcObject customMdcObject, boolean removeWhenScopeExits) {
        MdcObject mdcObject = getMdcManager().put(key, customMdcObject, removeWhenScopeExits);
        removeMdcObjectWhenScopeExits(removeWhenScopeExits, mdcObject);

        return mdcObject;
    }

    /**
     * Check if the provided mdc key is present
     * @return true if there is an existing key, false otherwise
     */
    @Beta
    public boolean isMdcKeyPresent(String key) {
        Object mdc = getMdcManager().getAll().get(key);
        return mdc != null;
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

    /**
     * Get the current analytics event. This can return null if analytics is not enabled.
     * @return
     */
    public Event getAnalyticsEvent() {
        return Scope.getCurrentScope().get(Attr.analyticsEvent, Event.class);
    }

    private static String generateDeploymentId() {
        long time = (new Date()).getTime();
        String dateString = String.valueOf(time);
        DecimalFormat decimalFormat = new DecimalFormat("0000000000");
        return dateString.length() > 9 ? dateString.substring(dateString.length() - 10) :
                decimalFormat.format(time);
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
