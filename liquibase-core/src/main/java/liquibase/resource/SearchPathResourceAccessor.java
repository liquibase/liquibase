package liquibase.resource;

import liquibase.GlobalConfiguration;
import liquibase.Scope;
import liquibase.util.StringUtil;

import java.io.IOException;

/**
 * This should generally be the overall {@link ResourceAccessor} used by integrations.
 * It aggregates integration-specific resource accessors with the standard {@link GlobalConfiguration#SEARCH_PATHS} setting
 * to create the overall "search path" for Liquibase.
 */
public class SearchPathResourceAccessor extends CompositeResourceAccessor {

    /**
     * Creates itself with the value in {@link GlobalConfiguration#SEARCH_PATHS}.
     */
    public SearchPathResourceAccessor(ResourceAccessor... additionalAccessors) {
        this(GlobalConfiguration.SEARCH_PATHS.getCurrentValue(), additionalAccessors);
    }

    /**
     * Creates itself with the given searchPath value.
     * If any of the paths in {@link GlobalConfiguration#SEARCH_PATHS} are invalid, an error is logged but no exception is thrown from this method.
     */
    public SearchPathResourceAccessor(String searchPath, ResourceAccessor... additionalAccessors) {
        if (searchPath != null) {
            final SearchPathHandlerFactory parserFactory = Scope.getCurrentScope().getSingleton(SearchPathHandlerFactory.class);
            StringUtil.splitAndTrim(searchPath, ",").forEach(path -> {
                try {
                    addResourceAccessor(parserFactory.getResourceAccessor(path));
                } catch (IOException e) {
                    Scope.getCurrentScope().getUI().sendMessage(e.getMessage());
                    Scope.getCurrentScope().getLog(getClass()).severe(e.getMessage(), e);
                }
            });
        }

        for (ResourceAccessor accessor : additionalAccessors) {
            this.addResourceAccessor(accessor);
        }

        String logMessage = "Overall search path: " + System.lineSeparator();
        for (String location : describeLocations()) {
            logMessage += "  - " + location + System.lineSeparator();
        }
        Scope.getCurrentScope().getLog(getClass()).fine(logMessage.trim());
    }
}
