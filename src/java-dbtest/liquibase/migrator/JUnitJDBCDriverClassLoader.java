package liquibase.migrator;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class JUnitJDBCDriverClassLoader extends URLClassLoader {
    public JUnitJDBCDriverClassLoader(String driverDir) throws Exception {
        super(getDriverClasspath(driverDir));
    }

    private static URL[] getDriverClasspath(String driverDir) throws Exception {
        File thisClassFile = new File(new URI(Thread.currentThread().getContextClassLoader().getResource("liquibase/migrator/JUnitJDBCDriverClassLoader.class").toExternalForm()));
        File jdbcLib = new File(thisClassFile.getParentFile().getParentFile().getParentFile().getParent(), "lib-jdbc");
        File[] driverJars = new File(jdbcLib, driverDir).listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith("jar");
            }
        });

        List<URL> urls = new ArrayList<URL>();
        for (File jar : driverJars) {
            urls.add(jar.toURL());
        }

        return urls.toArray(new URL[urls.size()]);
    }
}
