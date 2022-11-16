package liquibase.sdk.resource;

import liquibase.GlobalConfiguration;
import liquibase.resource.AbstractResourceAccessor;
import liquibase.resource.InputStreamList;
import liquibase.resource.Resource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.*;

public class MockResourceAccessor extends AbstractResourceAccessor {

    private SortedMap<String, String> contentByFileName;

    public MockResourceAccessor() {
        this(new HashMap<String, String>());
    }

    public MockResourceAccessor(Map<String, String> contentByFileName) {
        this.contentByFileName = new TreeMap<>(contentByFileName);
    }

    @Override
    public void close() throws Exception {

    }

    @Override
    public List<Resource> getAll(String path) throws IOException {
        path = path.replace("\\", "/");
        String content = contentByFileName.get(path);
        List<Resource> returnSet = new ArrayList<>();
        if (content != null) {
            returnSet.add(new MockResource(path, content));
        }

        if (returnSet.isEmpty()) {
            return null;
        }
        return returnSet;
    }

    @Override
    public List<Resource> search(String path, boolean recursive) throws IOException {
        path = path.replace("\\", "/");
        List<Resource> returnList = new ArrayList<>();
        for (String file : contentByFileName.keySet()) {
            if (file.startsWith(path)) {
                returnList.add(new MockResource(file, contentByFileName.get(file)));
            }
        }
        return returnList;
    }

    @Override
    public List<String> describeLocations() {
        return Collections.singletonList("MockResouceAccessor.java");
    }
}
