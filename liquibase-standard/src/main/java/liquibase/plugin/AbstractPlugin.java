package liquibase.plugin;

import liquibase.AbstractExtensibleObject;

/**
 * Convenience base class for Plugin implementations. Consider extending this rather than implementing Plugin for better future-compatibility.
 */
public abstract class AbstractPlugin extends AbstractExtensibleObject implements Plugin {
}
