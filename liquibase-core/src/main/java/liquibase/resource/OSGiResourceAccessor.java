package liquibase.resource;


import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.SortedSet;
import java.util.TreeSet;
import liquibase.Scope;
import liquibase.resource.InputStreamList;
import liquibase.resource.ResourceAccessor;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

public class OSGiResourceAccessor implements ResourceAccessor {

    private final Bundle bundle;
    private static final String FORWARD_SLASH = "/";
    private SortedSet<String> locations;

    public OSGiResourceAccessor(Class resourcesBundle) {
        bundle = FrameworkUtil.getBundle(resourcesBundle);
    }

    @Override
    public InputStreamList openStreams(String relativeTo, String streamPath) throws IOException {
        final Enumeration<URL> resources = bundle.getResources(getPath(relativeTo, streamPath));
        InputStreamList inputStreamList = new InputStreamList();
        while (resources != null && resources.hasMoreElements()) {
            final URL url = resources.nextElement();
            try {
                inputStreamList.add(url.toURI(), url.openStream());
            } catch (URISyntaxException e) {
                Scope.getCurrentScope().getLog(getClass()).severe(String.format("Failed to convert URL %s to URI", url), e);
            }
        }
        return inputStreamList;
    }

    @Override
    public InputStream openStream(String relativeTo, String streamPath) throws IOException {
        return bundle.getResource(getPath(relativeTo, streamPath)).openStream();
    }

    private String getPath(String relativeTo, String streamPath) {
        StringBuilder path = new StringBuilder();

        if (relativeTo != null) {
            final URL resource = bundle.getResource(relativeTo);
            if (resource != null) {
                path.append(Paths.get(resource.getFile()).getParent());
            }
        }
        path.append(streamPath);
        return path.toString();
    }

    @Override
    public SortedSet<String> list(String relativeTo, String path, boolean recursive, boolean includeFiles, boolean includeDirectories) throws IOException {
        final Enumeration<URL> resources = bundle.findEntries(getPath(relativeTo, path), null, recursive);
        SortedSet<String> list = new TreeSet<String>(){{add(FORWARD_SLASH);}};
        while (resources != null && resources.hasMoreElements()) {
            final URL url = resources.nextElement();
            final String filePath = url.getFile();
            if (includeFiles) {
                list.add(filePath);
            } else if (filePath.endsWith(FORWARD_SLASH)) {
                list.add(filePath);
            }
        }
        return list;
    }

    @Override
    public SortedSet<String> describeLocations() {
        try {
            if (locations == null) {
                locations = list(null, FORWARD_SLASH, true, false, true);
            }
            return locations;
        } catch (IOException e) {
            Scope.getCurrentScope().getLog(getClass()).severe("Failed to list all locations", e);
            return new TreeSet<>();
        }
    }
}
