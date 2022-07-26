package liquibase.resource;

import liquibase.AbstractExtensibleObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;

/**
 * Convenience base class for {@link ResourceAccessor} implementations.
 */
public abstract class AbstractResourceAccessor extends AbstractExtensibleObject implements ResourceAccessor {

//    @Override
//    public SortedSet<String> list(String relativeTo, String path, boolean recursive, boolean includeFiles, boolean includeDirectories) throws IOException {
//        SortedSet<String> returnList = new TreeSet<>();
//        for (Resource resource : find(relativeTo, path, recursive)) {
//            returnList.add(resource.getPath());
//        }
//        return returnList;
//    }

    @Override
    public InputStreamList openStreams(String relativeTo, String streamPath) throws IOException {
        InputStreamList returnList = new InputStreamList();
        for (Resource resource : list(resolve(relativeTo, streamPath), false)) {
            returnList.add(resource.getUri(), resource.openInputStream());
        }

        return returnList;
    }

    protected String resolve(String relativeTo, String path) {
        if (relativeTo == null) {
            return path;
        }
        return Paths.get(relativeTo).getParent().resolve(Paths.get(path)).toString();
    }

    @Override
    @java.lang.SuppressWarnings("squid:S2095")
    public InputStream openStream(String relativeTo, String streamPath) throws IOException {
        Resource resource = get(resolve(relativeTo, streamPath));
        if (resource == null) {
            return null;
        }
        return resource.openInputStream();
    }

}
