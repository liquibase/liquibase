package liquibase.resource;

import liquibase.GlobalConfiguration;
import liquibase.Scope;
import liquibase.util.StringUtil;

import java.io.IOException;

/**
 * {@link ResourceAccessor} that uses the value currently in {@link GlobalConfiguration#RESOURCE_ROOTS} at the time it is constructed.
 */
public class ResourceRootsResourceAccessor extends CompositeResourceAccessor {

    /**
     * Creates itself with the value in {@link GlobalConfiguration#RESOURCE_ROOTS}.
     */
    public ResourceRootsResourceAccessor() {
        this(GlobalConfiguration.RESOURCE_ROOTS.getCurrentValue());
    }

    /**
     * Creates itself with the given resourceRoots value.
     * If any of the paths in {@link GlobalConfiguration#RESOURCE_ROOTS} are invalid, an error is logged but no exception is thrown from this method.
     */
    public ResourceRootsResourceAccessor(String resourceRoots) {
        if (resourceRoots == null) {
            return;
        }

        final ResourceRootParserFactory parserFactory = Scope.getCurrentScope().getSingleton(ResourceRootParserFactory.class);
        StringUtil.splitAndTrim(resourceRoots, ",").forEach(path -> {
            try {
                addResourceAccessor(parserFactory.parse(path));
            } catch (IOException e) {
                Scope.getCurrentScope().getUI().sendMessage(e.getMessage());
                Scope.getCurrentScope().getLog(getClass()).severe(e.getMessage(), e);
            }
        });
    }
}
