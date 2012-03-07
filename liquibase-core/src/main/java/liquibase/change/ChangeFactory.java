package liquibase.change;

import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.servicelocator.ServiceLocator;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Factory class for constructing the correct liquibase.change.Change implementation based on the tag name.
 * It is currently implemented by a static array of Change implementations, although that may change in
 * later revisions.  The best way to get an instance of ChangeFactory is off the Liquibase.getChangeFactory() method.
 *
 * @see liquibase.change.Change
 */
public class ChangeFactory {

    private static ChangeFactory instance;

    private Map<String, SortedSet<Class<? extends Change>>> registry = new ConcurrentHashMap<String, SortedSet<Class<? extends Change>>>();

    private ChangeFactory() {
        Class<? extends Change>[] classes;
        try {
            classes = ServiceLocator.getInstance().findClasses(Change.class);

            for (Class<? extends Change> clazz : classes) {
                //noinspection unchecked
                register(clazz);
            }

        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }

    }

    /**
     * Return singleton ChangeFactory
     */
    public static synchronized ChangeFactory getInstance() {
        if (instance == null) {
             instance = new ChangeFactory();
        }
        return instance;
    }

    public static void reset() {
        instance = new ChangeFactory();
    }


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

    public void unregister(String name) {
        registry.remove(name);
    }

    public Map<String, SortedSet<Class<? extends Change>>> getRegistry() {
        return registry;
    }

    public Change create(String name) {
        SortedSet<Class <? extends Change>> classes = registry.get(name);

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
