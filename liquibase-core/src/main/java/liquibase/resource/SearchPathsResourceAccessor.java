package liquibase.resource;

import liquibase.GlobalConfiguration;
import liquibase.Scope;
import liquibase.util.StringUtil;

import java.io.IOException;

/**
 * {@link ResourceAccessor} that uses the value currently in {@link GlobalConfiguration#SEARCH_PATHS} at the time it is constructed.
 */
public class SearchPathsResourceAccessor extends CompositeResourceAccessor {

    /**
     * Creates itself with the value in {@link GlobalConfiguration#SEARCH_PATHS}.
     */
    public SearchPathsResourceAccessor() {
        this(GlobalConfiguration.SEARCH_PATHS.getCurrentValue());
    }

    /**
     * Creates itself with the given searchPaths value.
     * If any of the paths in {@link GlobalConfiguration#SEARCH_PATHS} are invalid, an error is logged but no exception is thrown from this method.
     */
    public SearchPathsResourceAccessor(String searchPaths) {
        if (searchPaths == null) {
            return;
        }

        final SearchPathParserFactory parserFactory = Scope.getCurrentScope().getSingleton(SearchPathParserFactory.class);
        StringUtil.splitAndTrim(searchPaths, ",").forEach(path -> {
            try {
                addResourceAccessor(parserFactory.parse(path));
            } catch (IOException e) {
                Scope.getCurrentScope().getUI().sendMessage(e.getMessage());
                Scope.getCurrentScope().getLog(getClass()).severe(e.getMessage(), e);
            }
        });
    }
}
