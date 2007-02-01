package liquibase.migrator.ant;

import liquibase.migrator.FileOpener;
import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Path;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

public class AntFileOpener implements FileOpener {
    private AntClassLoader loader;

    public AntFileOpener(Project project, Path classpath) {
        loader = new AntClassLoader(project, classpath);
    }

    public InputStream getResourceAsStream(String file) throws IOException {
        URL resource = loader.getResource(file);
        if (resource == null) {
            throw new IOException(file+" could not be found");
        }
        return resource.openStream();
    }

    public Enumeration<URL> getResources(String packageName) throws IOException {
        return loader.getResources(packageName);
    }
}
