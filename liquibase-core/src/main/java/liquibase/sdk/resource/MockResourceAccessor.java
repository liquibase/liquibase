package liquibase.sdk.resource;

import liquibase.GlobalConfiguration;
import liquibase.resource.AbstractResourceAccessor;
import liquibase.resource.InputStreamList;
import liquibase.resource.Resource;
import liquibase.resource.UnknownResource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.*;

public class MockResourceAccessor extends AbstractResourceAccessor {

    private Map<String, String> contentByFileName;

    public MockResourceAccessor() {
        this(new HashMap<String, String>());
    }

    public MockResourceAccessor(Map<String, String> contentByFileName) {
        this.contentByFileName = contentByFileName;
    }


    @Override
    public InputStreamList openStreams(String relativeTo, String streamPath) throws IOException {
        InputStream stream = null;
        if (contentByFileName.containsKey(streamPath)) {
            stream = new ByteArrayInputStream(contentByFileName.get(streamPath).getBytes(GlobalConfiguration.OUTPUT_FILE_ENCODING.getCurrentValue()));
        }
        if (stream == null) {
            return null;
        } else {
            InputStreamList list = new InputStreamList();
            list.add(URI.create(streamPath), stream);
            return list;
        }
    }

    @Override
    public List<Resource> find(String relativeTo, String path, boolean recursive, boolean includeFiles, boolean includeDirectories) throws IOException {
        List<Resource> returnSet = new ArrayList<>();
        for (String file : contentByFileName.keySet()) {
            if (file.startsWith(path)) {
                returnSet.add(new UnknownResource(file));
            }
        }
        return returnSet;
    }

    @Override
    public SortedSet<String> describeLocations() {
        return new TreeSet<String>(Collections.singletonList("MockResouceAccessor.java"));
    }
}
