package liquibase.resource;

import liquibase.GlobalConfiguration;
import liquibase.Scope;
import liquibase.logging.Logger;
import liquibase.util.StringUtil;

import java.io.IOException;

/**
 * This should generally be the overall {@link ResourceAccessor} used by integrations.
 * It aggregates integration-specific resource accessors with the standard {@link GlobalConfiguration#SEARCH_PATH} setting
 * to create the overall "search path" for Liquibase.
 */
public class SearchPathResourceAccessor extends CompositeResourceAccessor {

    /**
     * Calls {@link #SearchPathResourceAccessor(String, ResourceAccessor...)} with the current value of {@link GlobalConfiguration#SEARCH_PATH}.
     */
    public SearchPathResourceAccessor(ResourceAccessor... defaultAccessors) {
        this(GlobalConfiguration.SEARCH_PATH.getCurrentValue(), defaultAccessors);
    }

    /**
     * Creates itself with the given searchPath value.
     * If any of the paths in {@link GlobalConfiguration#SEARCH_PATH} are invalid, an error is logged but no exception is thrown from this method.
     *
     * @param defaultAccessors Only uses these accessors if searchPath is null.
     */
    public SearchPathResourceAccessor(String searchPath, ResourceAccessor... defaultAccessors) {

        final Logger log = Scope.getCurrentScope().getLog(getClass());
        if (searchPath == null) {
            for (ResourceAccessor accessor : defaultAccessors) {
                this.addResourceAccessor(accessor);
            }
        } else {
            final PathHandlerFactory parserFactory = Scope.getCurrentScope().getSingleton(PathHandlerFactory.class);
            StringUtil.splitAndTrim(searchPath, ",").forEach(path -> {
                try {
                    addResourceAccessor(parserFactory.getResourceAccessor(path));
                } catch (IOException e) {
                    Scope.getCurrentScope().getUI().sendMessage(e.getMessage());
                    log.severe(e.getMessage(), e);
                }
            });
        }

        final StringBuilder logMessage = new StringBuilder("Overall search path: " + System.lineSeparator());
        for (String location : describeLocations()) {
            logMessage.append("  - ").append(location).append(System.lineSeparator());
        }
        log.fine(logMessage.toString().trim());
    }

    /**
     * Adds the given root as a new resource accessor, using {@link PathHandlerFactory} to find the right {@link PathHandler}.
     */
    public SearchPathResourceAccessor addResourceAccessor(String root) throws IOException {
        final PathHandlerFactory parserFactory = Scope.getCurrentScope().getSingleton(PathHandlerFactory.class);
        return (SearchPathResourceAccessor) super.addResourceAccessor(parserFactory.getResourceAccessor(root));
    }
}
