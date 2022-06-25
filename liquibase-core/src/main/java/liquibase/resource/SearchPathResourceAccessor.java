package liquibase.resource;

import liquibase.GlobalConfiguration;
import liquibase.Scope;
import liquibase.util.StringUtil;

import java.io.IOException;

/**
 * {@link ResourceAccessor} that uses the value currently in {@link GlobalConfiguration#SEARCH_PATHS} at the time it is constructed.
 */
public class SearchPathResourceAccessor extends CompositeResourceAccessor {

    /**
     * Creates itself with the value in {@link GlobalConfiguration#SEARCH_PATHS}.
     */
    public SearchPathResourceAccessor() {
        this(GlobalConfiguration.SEARCH_PATHS.getCurrentValue());
    }

    /**
     * Creates itself with the given searchPath value.
     * If any of the paths in {@link GlobalConfiguration#SEARCH_PATHS} are invalid, an error is logged but no exception is thrown from this method.
     */
    public SearchPathResourceAccessor(String searchPath) {
        if (searchPath == null) {
            return;
        }

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
}
