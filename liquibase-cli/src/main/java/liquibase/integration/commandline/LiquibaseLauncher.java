package liquibase.integration.commandline;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LiquibaseLauncher {

    private static boolean debug = false;

    public static void main(String[] args) throws Exception {

        final String debugSetting = System.getenv("LIQUIBASE_LAUNCHER_DEBUG");
        if (debugSetting != null && debugSetting.equals("true")) {
            LiquibaseLauncher.debug = true;
            debug("Debug mode enabled because LIQUBIASE_LAUNCHER_DEBUG is set to " + debugSetting);
        }

        final String liquibaseHomeEnv = System.getenv("LIQUIBASE_HOME");
        debug("LIQUIBASE_HOME: " + liquibaseHomeEnv);
        if (liquibaseHomeEnv == null || liquibaseHomeEnv.equals("")) {
            throw new IllegalArgumentException("Unable to find LIQUIBASE_HOME environment variable");
        }
        File liquibaseHome = new File(liquibaseHomeEnv);

        List<URL> urls = new ArrayList<>();
        urls.add(new File(liquibaseHome, "liquibase.jar").toURI().toURL());



        File[] libDirs = new File[]{
                new File("./liquibase_libs"),
                new File(liquibaseHome, "lib"),
                new File(liquibaseHome, "internal/lib"),
        };

        for (File libDirFile : libDirs) {
            debug("Looking for libraries in " + libDirFile.getAbsolutePath());

            if (!libDirFile.exists()) {
                debug("Skipping directory " + libDirFile.getAbsolutePath() + " because it does not exist");
                continue;
            }
            final File[] files = libDirFile.listFiles();
            if (files == null) {
                debug("Skipping directory " + libDirFile.getAbsolutePath() + " because it does not list files");
                continue;
            }

            for (File lib : files) {
                if (lib.getName().toLowerCase(Locale.US).endsWith(".jar")) {
                    try {
                        urls.add(lib.toURI().toURL());
                        debug("Added " + lib.getAbsolutePath() + " to classpath");
                    } catch (Exception e) {
                        debug("Error adding " + lib.getAbsolutePath() + ":" + e.getMessage(), e);
                    }
                }
            }

            //add the dir itself
            try {
                urls.add(libDirFile.toURI().toURL());
                debug("Added " + libDirFile.getAbsolutePath() + " to classpath");
            } catch (Exception e) {
                debug("Error adding " + libDirFile.getAbsolutePath() + ":" + e.getMessage(), e);
            }
        }

        if (debug) {
            debug("Final Classpath:");
            for (URL url : urls) {
                debug("  " + url.toString());
            }
        }

        final URLClassLoader classloader = new URLClassLoader(urls.toArray(new URL[0]), null);
        Thread.currentThread().setContextClassLoader(classloader);

        final Class<?> cli = Class.forName("liquibase.integration.commandline.LiquibaseCommandLine", false, classloader);

        String[] argsToPass = new String[0];
        if (args != null && args.length > 0) {
            argsToPass = args;
        }

        cli.getMethod("main", String[].class).invoke(null, new Object[] {argsToPass});
    }

    private static void debug(String message) {
        if (debug) {
            debug(message, null);
        }
    }

    private static void debug(String message, Throwable e) {
        if (debug) {
            System.err.println("[LIQUIBASE LAUNCHER DEBUG] " + message);

            if (e != null) {
                e.printStackTrace(System.err);
            }
        }
    }
}
