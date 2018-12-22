package liquibase.resource;

import liquibase.AbstractExtensibleObject;
import liquibase.exception.LiquibaseException;
import liquibase.util.CollectionUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * A {@link liquibase.resource.ResourceAccessor} that contains multiple sub-accessors and combines the results of all of them.
 */
public class CompositeResourceAccessor extends AbstractResourceAccessor {

    private List<ResourceAccessor> resourceAccessors;

    public CompositeResourceAccessor(ResourceAccessor... resourceAccessors) {
        this.resourceAccessors = Arrays.asList(CollectionUtil.createIfNull(resourceAccessors));
    }

    public CompositeResourceAccessor(Collection<ResourceAccessor> resourceAccessors) {
        this.resourceAccessors = new ArrayList<>(resourceAccessors);
    }

    @Override
    public InputStreamList openStreams(String path) throws IOException {
        InputStreamList returnList = new InputStreamList();
        for (ResourceAccessor accessor : resourceAccessors) {
            returnList.addAll(accessor.openStreams(path));
        }
        return returnList;
    }

    @Override
    public SortedSet<String> list(String path, boolean recursive, boolean includeFiles, boolean includeDirectories) throws IOException {
        SortedSet<String> returnSet = new TreeSet<>();
        for (ResourceAccessor accessor : resourceAccessors) {
            returnSet.addAll(accessor.list(path, recursive, includeFiles, includeDirectories));
        }

        return returnSet;
    }

    @Override
    public String getCanonicalPath(String relativeTo, String path) throws IOException {
        for (ResourceAccessor resourceAccessor : resourceAccessors) {
            String canonicalPath = resourceAccessor.getCanonicalPath(relativeTo, path);
            if (canonicalPath != null) {
                return canonicalPath;
            }
        }

        return null;
    }
}
