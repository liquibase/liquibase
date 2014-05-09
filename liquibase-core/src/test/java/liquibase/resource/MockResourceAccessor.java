package liquibase.resource;

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

    public InputStream getResourceAsStream(String file) throws IOException {
        if (contentByFileName.containsKey(file)) {
            return new ByteArrayInputStream(contentByFileName.get(file).getBytes());
        } else if (file.startsWith("file:///")) {
            return getResourceAsStream(file.replaceFirst("file:///", ""));
        }
        return null;
    }

    public Enumeration<URL> getResources(String packageName) throws IOException {
        Vector<URL> urls = new Vector<URL>();
        for (String file : contentByFileName.keySet()) {
            if (file.startsWith(packageName)) {
                String urlName = file;
                if (!urlName.contains(":")) {
                    urlName = "file:///"+urlName;
                }
                urls.add(new URL(urlName));
            }
        }
        return urls.elements();
    }

    public ClassLoader toClassLoader() {
        return null;
    }
}
