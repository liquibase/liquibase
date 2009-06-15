package liquibase.test;

import liquibase.resource.ResourceAccessor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;

public class JUnitResourceAccessor implements ResourceAccessor {
    private URLClassLoader classLoader;

    public JUnitResourceAccessor() throws Exception {
        File thisClassFile = new File(new URI(this.getClass().getClassLoader().getResource("liquibase/test/JUnitResourceAccessor.class").toExternalForm()));
        File srcDir = new File(thisClassFile.getParentFile().getParentFile().getParentFile().getParent(), "src");
        classLoader = new URLClassLoader(new URL[]{
                new File(srcDir, "samples").toURL(),
                new File(srcDir, "java-test").toURL(),
                new File(System.getProperty("java.io.tmpdir")).toURL(),

        });

    }

    public InputStream getResourceAsStream(String file) throws IOException {
        return classLoader.getResourceAsStream(file);
    }

    public Enumeration<URL> getResources(String packageName) throws IOException {
        return classLoader.getResources(packageName);
    }

    public ClassLoader toClassLoader() {
        return classLoader;
    }
}
