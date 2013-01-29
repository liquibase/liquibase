package liquibase.change;

import liquibase.exception.UnexpectedLiquibaseException;
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

    private Map<String, SortedSet<Class<? extends Change>>> registry = new ConcurrentHashMap<String, SortedSet<Class<? extends Change>>>();

    private ChangeFactory() {
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
        }
        return instance;
    }

    /**
     * Reset the ChangeFactory so it reloads the registry on the next call to @{link #getInstance()}. Mainly used in testing
     */
    public static void reset() {
        instance = null;
    }


    /**
     * Register a new Change class.
     * Normally called automatically by ChangeFactory on all Change implementations found by the ServiceLocator, but it can be called manually if needed.
     */
    public void register(Class<? extends Change> changeClass) {
        try {
            String name = changeClass.newInstance().getChangeMetaData().getName();
            if (registry.get(name) == null) {
                registry.put(name, new TreeSet<Class<? extends Change>>(new Comparator<Class<? extends Change>>() {
                    public int compare(Class<? extends Change> o1, Class<? extends Change> o2) {
                        try {
                            return -1 * new Integer(o1.newInstance().getChangeMetaData().getPriority()).compareTo(o2.newInstance().getChangeMetaData().getPriority());
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
     * Clear the registry of all Changes found. Normally used for testing.
     */
    public void clear() {
        registry.clear();
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

}
