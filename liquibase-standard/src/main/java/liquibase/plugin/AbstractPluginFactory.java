package liquibase.plugin;

import liquibase.Scope;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.servicelocator.PrioritizedService;
import liquibase.servicelocator.ServiceLocator;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Convenience base class for all factories that find correct {@link Plugin} implementations.
 */
public abstract class AbstractPluginFactory<T extends Plugin> implements PluginFactory {

    private volatile Collection<T> allInstances;

    protected AbstractPluginFactory() {

    }

    protected abstract Class<T> getPluginClass();

    /**
     * Finds the plugin filtering and ordering with the passed {@link PriorityArgsClosure}.
     * This method is called by a public implementation-specific methods.
     * Normally this does not need to be overridden, instead define an implementor of {@link PriorityArgsClosure}
     * to compute the priority of each object for the scope and arguments passed to this method.
     * <p>
     * However, if there is a {@link Scope} key of "liquibase.plugin.${plugin.interface.class.Name}",
     * an instance of that class is returned or {@link RuntimeException} is thrown.
     *
     * @throws RuntimeException when cannot find registred plugin for specified name.
     * @return null if no plugins are found or have a priority greater than zero.
     */
    protected T getPlugin(final PriorityArgsClosure<? super T> chooser) {
        Set<T> applicable = this.getPlugins(chooser);

        if (applicable.isEmpty()) {
            return null;
        }
        return applicable.iterator().next();
    }

    protected Set<T> getPlugins(final PriorityArgsClosure<? super T> chooser) {
        Optional<T> forcedPlugin = this.getForcedPluginIfAvailable();
        if (forcedPlugin.isPresent()) {
            return new HashSet<>(Collections.singletonList(forcedPlugin.get()));
        }

        return findAllInstances()
                .stream()
                .filter(chooser)
                .sorted(chooser)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Optional<T> getForcedPluginIfAvailable() {
        final String pluginClassName = getPluginClass().getName();
        final Class<?> forcedPlugin = Scope.getCurrentScope().get("liquibase.plugin." + pluginClassName, Class.class);
        if (forcedPlugin != null) {
            for (T plugin : findAllInstances()) {
                if (plugin.getClass().equals(forcedPlugin)) {
                    return Optional.of(plugin);
                }
            }
            throw new UnexpectedLiquibaseException("No registered " + pluginClassName + " plugin " + forcedPlugin.getName());
        }
        return Optional.empty();
    }


    public void register(T plugin) {
        this.findAllInstances();
        this.allInstances.add(plugin);
    }

    /**
     * Finds implementations of the given interface or class and returns instances of them.
     * Standard implementation uses {@link ServiceLoader} to find implementations and caches results in {@link #allInstances} which means the same objects are always returned.
     * If the instances should not be treated as singletons, clone the objects before returning them from {@link #getPlugin(PriorityArgsClosure)}.
     */
    protected Collection<T> findAllInstances() {
        if (this.allInstances == null) {
            synchronized (this) {
                if (this.allInstances == null) {
                    ServiceLocator serviceLocator = Scope.getCurrentScope().getServiceLocator();
                    this.allInstances = new CopyOnWriteArrayList<>(serviceLocator.findInstances(getPluginClass()));
                }
            }
        }

        return this.allInstances;
    }

    protected void removeInstance(T instance) {
        if (this.allInstances == null) {
            return;
        }
        this.allInstances.remove(instance);
    }

    /**
     * Allows to influence selection of a plugin per request.
     *
     * @param <T> same as PluginFactory's.
     */
    @FunctionalInterface
    public interface PriorityArgsClosure<T extends Plugin> extends Comparator<T>, Predicate<T> {
        /**
         * Highest wins, negatives are not considered.
         *
         * @param obj to assess
         * @return value, influencing filtering and comparison
         */
        int getPriority(T obj);

        @Override
        default int compare(T o1, T o2) {
            //Reversed!
            int i = Integer.compare(getPriority(o2), getPriority(o1));
            if (i == 0) {
                //Not reversed!
                return o1.getClass().getName().compareTo(o2.getClass().getName());
            }
            return i;
        }

        @Override
        default boolean test(T t) {
            return getPriority(t) >= 0;
        }
    }

    public static final PriorityArgsClosure<PrioritizedService> PLAIN_PRIORITIZED_SERVICE = PrioritizedService::getPriority;
}
