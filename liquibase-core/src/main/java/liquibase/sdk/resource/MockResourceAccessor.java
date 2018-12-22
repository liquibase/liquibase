package liquibase.sdk.resource;

import liquibase.configuration.GlobalConfiguration;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.resource.AbstractResourceAccessor;
import liquibase.resource.InputStreamList;
import liquibase.resource.ResourceAccessor;

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
    public InputStreamList openStreams(String path) throws IOException {
        InputStream stream = null;
        if (contentByFileName.containsKey(path)) {
            stream = new ByteArrayInputStream(contentByFileName.get(path).getBytes(LiquibaseConfiguration.getInstance().getConfiguration(GlobalConfiguration.class).getOutputEncoding()));
        }
        if (stream == null) {
            return null;
        } else {
            InputStreamList list = new InputStreamList();
            list.add(URI.create(path), stream);
            return list;
        }
    }

    @Override
    public SortedSet<String> list(String path, boolean recursive, boolean includeFiles, boolean includeDirectories) throws IOException {
        SortedSet<String> returnSet = new TreeSet<>();
        for (String file : contentByFileName.keySet()) {
            if (file.startsWith(path)) {
                returnSet.add(file);
            }
        }
        return returnSet;
    }

    @Override
    public String getCanonicalPath(String relativeTo, String path) throws IOException {
        return path;
    }
}
