package liquibase.sdk.supplier.database;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

public class JDBCDriverClassLoader extends URLClassLoader {

    public JDBCDriverClassLoader() {
        super(getDriverClasspath());
    }

    private static URL[] getDriverClasspath() {
        try {
            List<URL> urls = new ArrayList<URL>();

            addUrlsFromPath(urls,  "drivers");

            return urls.toArray(new URL[urls.size()]);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void addUrlsFromPath(List<URL> addTo,String path) throws Exception{
        File jdbcLib = null;
        try {
            URL url = JDBCDriverClassLoader.class.getClassLoader().getResource(path);
            if (url == null) {
                System.out.println("Null URL for "+path);
                return;
            } else {
                jdbcLib = new File(url.toURI());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        if (!jdbcLib.exists()) {
                throw new RuntimeException("JDBC driver directory "+jdbcLib.getAbsolutePath()+" does not exist");
            }
            File[] files = jdbcLib.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.isDirectory();
                }
            });
            if(files == null) {
                files = new File[]{};
            }
            for (File driverDir : files) {
                File[] driverJars = driverDir.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return name.endsWith("jar");
                    }
                });

                for (File jar : driverJars) {
                    addTo.add(jar.toURL());
                }

            }
    }
}
