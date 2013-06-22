package liquibase.test;

import liquibase.resource.ResourceAccessor;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

public class MockResourceAccessor implements ResourceAccessor {
    public InputStream getResourceAsStream(String file) throws IOException {
        return null;
    }

    public Enumeration<URL> getResources(String packageName) throws IOException {
        return null;
    }

    public ClassLoader toClassLoader() {
        return null;
    }
}
