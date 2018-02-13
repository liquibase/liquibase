package liquibase.plugin;

import liquibase.Scope;
import liquibase.servicelocator.ServiceLocator;

import java.util.*;

/**
 * Convenience base class for all factories that find correct {@link Plugin} implementations.
 */
public abstract class AbstractPluginFactory<T extends Plugin> implements PluginFactory {

    private Collection<T> allInstances;

    protected AbstractPluginFactory() {

    }

    protected abstract Class<T> getPluginClass();

    /**
     * Returns the priority of the given object based on the passed args array.
     * The args are created as as part of the custom public getPlugin method in implementations are are passed through {@link #getPlugin(Object...)}
     */
    protected abstract int getPriority(T obj, Object... args);

    /**
     * Finds the plugin for which {@link #getPriority(Plugin, Object...)}  returns the highest value for the given scope and args.
     * This method is called by a public implementation-specific methods.
     * Normally this does not need to be overridden, instead override {@link #getPriority(Plugin, Object...)} to compute the priority of each object for the scope and arguments passed to this method.
     *
     * @return null if no plugins are found or have a priority greater than zero.
     */
    protected T getPlugin(final Object... args) {
        TreeSet<T> applicable = new TreeSet<>((o1, o2) -> {
            Integer o1Priority = getPriority(o1, args);
            Integer o2Priority = getPriority(o2, args);

            int i = o2Priority.compareTo(o1Priority);
            if (i == 0) {
                return o1.getClass().getName().compareTo(o2.getClass().getName());
            }
            return i;
        });

        for (T plugin : findAllInstances()) {
            if (getPriority(plugin, args) >= 0) {
                applicable.add(plugin);
            }
        }

        if (applicable.size() == 0) {
            return null;
        }

        return applicable.iterator().next();

    }


    public void register(T plugin){
        this.findAllInstances();
        this.allInstances.add(plugin);
    }

    /**
     * Finds implementations of the given interface or class and returns instances of them.
     * Standard implementation uses {@link ServiceLoader} to find implementations and caches results in {@link #allInstances} which means the same objects are always returned.
     * If the instances should not be treated as singletons, clone the objects before returning them from {@link #getPlugin(Object...)}.
     */
    protected synchronized Collection<T> findAllInstances() {
        if (this.allInstances == null) {
            this.allInstances = new ArrayList<>();

            ServiceLocator serviceLocator = Scope.getCurrentScope().getServiceLocator();
            this.allInstances.addAll(serviceLocator.findInstances(getPluginClass()));
        }

        return this.allInstances;
    }

    protected void removeInstance(T instance) {
        if (this.allInstances == null) {
            return;
        }
        this.allInstances.remove(instance);
    }

}
