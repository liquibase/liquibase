package liquibase.migrator;

import java.net.URLClassLoader;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.File;
import java.io.FilenameFilter;
import java.util.List;
import java.util.ArrayList;

public class JUnitJDBCDriverClassLoader extends URLClassLoader {
    public JUnitJDBCDriverClassLoader(String driverDir) throws Exception {
        super(getDriverClasspath(driverDir));
    }

    private static URL[] getDriverClasspath(String driverDir) throws Exception{
        File thisClassFile = new File(Thread.currentThread().getContextClassLoader().getResource("liquibase/migrator/JUnitJDBCDriverClassLoader.class").toURI());
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
