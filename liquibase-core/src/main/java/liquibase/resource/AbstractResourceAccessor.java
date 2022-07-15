package liquibase.resource;

import liquibase.AbstractExtensibleObject;
import liquibase.GlobalConfiguration;
import liquibase.Scope;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.logging.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Convenience base class for {@link ResourceAccessor} implementations.
 */
public abstract class AbstractResourceAccessor extends AbstractExtensibleObject implements ResourceAccessor {

    @Override
    public SortedSet<String> list(String relativeTo, String path, boolean recursive, boolean includeFiles, boolean includeDirectories) throws IOException {
        SortedSet<String> returnList = new TreeSet<>();
        for (Resource resource : find(relativeTo, path, recursive, includeFiles, includeDirectories)) {
            returnList.add(resource.getPath());
        }
        return returnList;
    }

    @Override
    public InputStreamList openStreams(String relativeTo, String streamPath) throws IOException {
        InputStreamList returnList = new InputStreamList();
        for (Resource resource : find(relativeTo, streamPath, false, true, false)) {
            returnList.add(resource.getDescription(), resource.openInputStream());
        }

        return returnList;
    }

    @Override
    @java.lang.SuppressWarnings("squid:S2095")
    public InputStream openStream(String relativeTo, String streamPath) throws IOException {
        Resource resource = find(relativeTo, streamPath);
        if (resource == null) {
            return null;
        }
        return resource.openInputStream();
    }
}
