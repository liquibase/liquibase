package liquibase.plugin;

import liquibase.SingletonObject;

/**
 * Interface for classes that manage {@link Plugin}s.
 * Normally the factories will use a "priority" mechanism where they assign a priority to each object and return the plugin object with the highest priority.
 */
public interface PluginFactory extends SingletonObject {
}
