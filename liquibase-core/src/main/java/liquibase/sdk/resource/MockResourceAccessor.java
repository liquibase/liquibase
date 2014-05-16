package liquibase.sdk.resource;

import liquibase.resource.ResourceAccessor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

public class MockResourceAccessor implements ResourceAccessor {

    private Map<String, String> contentByFileName;

    public MockResourceAccessor() {
        this(new HashMap<String, String>());
    }

    public MockResourceAccessor(Map<String, String> contentByFileName) {
        this.contentByFileName = contentByFileName;
    }

    @Override
    public Set<InputStream> getResourcesAsStream(String path) throws IOException {
        InputStream stream = null;
        if (contentByFileName.containsKey(path)) {
            stream = new ByteArrayInputStream(contentByFileName.get(path).getBytes());
        }
        if (stream == null) {
            return null;
        } else {
            return new HashSet<InputStream>(Arrays.asList(stream));
        }
    }

    @Override
    public Set<String> list(String relativeTo, String path, boolean includeFiles, boolean includeDirectories, boolean recursive) throws IOException {
        Set<String> returnSet = new HashSet<String>();
        for (String file : contentByFileName.keySet()) {
            if (file.startsWith(path)) {
                returnSet.add(file);
            }
        }
        return returnSet;
    }

    public ClassLoader toClassLoader() {
        return null;
    }
}
