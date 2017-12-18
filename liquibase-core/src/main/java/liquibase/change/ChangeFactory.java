package liquibase.change;

import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.logging.LogService;
import liquibase.logging.Logger;
import liquibase.servicelocator.ServiceLocator;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Factory class for constructing the correct liquibase.change.Change implementation based on a command name.
 * For XML-based changelogs, the tag name is the command name.
 * Change implementations are looked up via the {@link ServiceLocator}.
 *
 * @see liquibase.change.Change
 */
public class ChangeFactory {

    private static ChangeFactory instance;

    private Map<String, SortedSet<Class<? extends Change>>> registry = new ConcurrentHashMap<>();
    private Map<Class<? extends Change>, ChangeMetaData> metaDataByClass = new ConcurrentHashMap<>();

    private Logger log;

    private ChangeFactory() {
      log = LogService.getLog(getClass());
    }

    protected Logger getLogger() {
      return log;
    }
    
    private void init() {
        Class<? extends Change>[] classes;
        classes = ServiceLocator.getInstance().findClasses(Change.class);

        for (Class<? extends Change> clazz : classes) {
            //noinspection unchecked
            register(clazz);
        }
    }

    /**
     * Return the singleton ChangeFactory instance.
     */
    public static synchronized ChangeFactory getInstance() {
        if (instance == null) {
            instance = new ChangeFactory();
            instance.init();
        }
        return instance;
    }

    /**
     * Reset the ChangeFactory so it reloads the registry on the next call to @{link #getInstance()}. Mainly used in testing
     */
    public static synchronized void reset() {
        instance = null;
    }


    /**
     * Register a new Change class.
     * Normally called automatically by ChangeFactory on all Change implementations found by the ServiceLocator, but it can be called manually if needed.
     */
    public void register(Class<? extends Change> changeClass) {
        try {
            Change instance = changeClass.newInstance();
            ChangeMetaData metaData = getChangeMetaData(instance);
            String name = metaData.getName();
            if (registry.get(name) == null) {
                registry.put(name, new TreeSet<>(new Comparator<Class<? extends Change>>() {
                    @Override
                    public int compare(Class<? extends Change> o1, Class<? extends Change> o2) {
                        try {
                            return -1 * Integer.valueOf(getChangeMetaData(o1.newInstance()).getPriority()).compareTo(getChangeMetaData(o2.newInstance()).getPriority());
                        } catch (Exception e) {
                            throw new UnexpectedLiquibaseException(e);
                        }
                    }
                }));
            }
            registry.get(name).add(changeClass);
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    public ChangeMetaData getChangeMetaData(String change) {
        Change changeObj = create(change);
        if (changeObj == null) {
            return null;
        }
        return getChangeMetaData(changeObj);
    }

    public ChangeMetaData getChangeMetaData(Change change) {
        if (!metaDataByClass.containsKey(change.getClass())) {
            metaDataByClass.put(change.getClass(), change.createChangeMetaData());
        }
        return metaDataByClass.get(change.getClass());
    }

    /**
     * Unregister all instances of a given Change name. Normally used for testing, but can be called manually if needed.
     */
    public void unregister(String name) {
        registry.remove(name);
    }

    /**
     * Return the registry of all Changes found. Key is the change name and the values are a sorted set of implementations, ordered by Priority descending.
     * Normally used only for information/debugging purposes. The returned map is read only.
     */
    public Map<String, SortedSet<Class<? extends Change>>> getRegistry() {
        return Collections.unmodifiableMap(registry);
    }


    /**
     * Returns all defined changes in the registry. Returned set is not modifiable.
     */
    public Set<String> getDefinedChanges() {
        return Collections.unmodifiableSet(registry.keySet());
    }

    /**
     * Clear the registry of all Changes found. Normally used for testing.
     */
    public void clear() {
        registry.clear();
        metaDataByClass.clear();
    }

    /**
     * Create a new Change implementation for the given change name. The class of the constructed object will be the Change implementation with the highest priority.
     * Each call to create will return a new instance of the Change.
     */
    public Change create(String name) {
        SortedSet<Class<? extends Change>> classes = registry.get(name);

        if (classes == null) {
            return null;
        }

        try {
            return classes.iterator().next().newInstance();
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    public String[] getAllChangeNamespaces() {
        Set<String> namespaces = new HashSet<>();
        for (String changeName : getDefinedChanges()) {
            Change change = create(changeName);
            namespaces.add(change.getSerializedObjectNamespace());
        }

        return namespaces.toArray(new String[namespaces.size()]);
    }

    public Map<String, Object> getParameters(Change change) {
        Map<String, Object> returnMap = new HashMap<>();
        ChangeMetaData changeMetaData = getChangeMetaData(change);
        for (ChangeParameterMetaData param : changeMetaData.getParameters().values()) {
            Object currentValue = param.getCurrentValue(change);
            if (currentValue != null) {
                returnMap.put(param.getParameterName(), currentValue);
            }
        }

        return returnMap;
    }

    // exposed for test only
    protected void setLogger(Logger mockLogger) {
      log = mockLogger;
    }
}
