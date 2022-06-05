package liquibase.resource;

import liquibase.plugin.Plugin;

import java.io.IOException;

/**
 * Interface for plugins that can create {@link ResourceAccessor}s out of the values in {@link liquibase.GlobalConfiguration#SEARCH_PATHS}.
 */
public interface SearchPathParser extends Plugin {

    /**
     * Priority of this parser for the given path. The implementation with the highest priority will be used.
     */
    int getPriority(String root);

    /**
     * Parse the given path and return a {@link ResourceAccessor} for it.
     *
     * @throws IOException if the path is invalid
     */
    ResourceAccessor parse(String root) throws IOException;
}
