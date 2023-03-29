package liquibase.plugin;

/**
 * Interface for objects designed to be pluggable in Liquibase.
 * Implementations of this class should be looked up via factories rather than instantiated directly to support the extension system.
 * <br><br>
 * The normal pattern for plugins is for {@link PluginFactory} implementations to return them.
 */
public interface Plugin {

    /**
     * Value to return from priority functions when the plugin is not applicable.
     */
    int PRIORITY_NOT_APPLICABLE = -1;

    /**
     * Value to return from priority functions when the plugin is the standard implementation.
     */
    int PRIORITY_DEFAULT = 1;

    /**
     * Value to return from priority functions when the plugin is a specialized, non-default implementation.
     */
    int PRIORITY_SPECIALIZED = 10;


}
