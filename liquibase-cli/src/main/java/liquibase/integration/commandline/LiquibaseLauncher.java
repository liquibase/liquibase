package liquibase.integration.commandline;

import static liquibase.integration.commandline.LiquibaseLauncherSettings.LiquibaseLauncherSetting.LIQUIBASE_HOME;
import static liquibase.integration.commandline.LiquibaseLauncherSettings.LiquibaseLauncherSetting.LIQUIBASE_LAUNCHER_DEBUG;
import static liquibase.integration.commandline.LiquibaseLauncherSettings.LiquibaseLauncherSetting.LIQUIBASE_LAUNCHER_PARENT_CLASSLOADER;
import static liquibase.integration.commandline.LiquibaseLauncherSettings.getSetting;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Launcher which builds up the classpath needed to run Liquibase, then calls {@link LiquibaseCommandLine#main(String[])}.
 * <p>
 * Supports the following configuration options that can be passed as JVM properties and/or environment variables, taking
 * the former precedence over the latter:
 * <table>
 *   <thead>
 *     <tr>
 *       <th><b>Environment variable</b></th>
 *       <th><b>JVM property</b></th>
 *     </tr>
 *     <tr>
 *       <th colspan="2"><b>Meaning</b></th>
 *     </tr>
 *   </thead>
 *   <tbody>
 *     <tr>
 *       <td><code>LIQUIBASE_HOME</code></td>
 *       <td><code>liquibase.home</code></td>
 *     </tr>
 *     <tr>
 *       <td colspan="2">Liquibase home. This option is mandatory.</td>
 *     </tr>
 *     <tr>
 *       <td><code>LIQUIBASE_LAUNCHER_DEBUG</code></td>
 *       <td><code>liquibase.launcher.debug</code></td>
 *     </tr>
 *     <tr>
 *       <td colspan="2">Determine if it should, when <code>true</code>, log what it is doing to <code>stderr</code>.
 *       Defaults to <code>false</code>.</td>
 *     </tr>
 *     <tr>
 *       <td><code>LIQUIBASE_LAUNCHER_PARENT_CLASSLOADER</code></td>
 *       <td><code>liquibase.launcher.parent_classloader</code></td>
 *     </tr>
 *     <tr>
 *       <td colspan="2">Classloader that will be used to run Liquibase, either <code>system</code> or <code>thread</code>.
 *       Defaults to <code>system</code>.</td>
 *     </tr>
 *   </tbody>
 * </table>
 */
public class LiquibaseLauncher {

    private static boolean debug = false;

    public static void main(final String[] args) throws Exception {

        final String debugSetting = getSetting(LIQUIBASE_LAUNCHER_DEBUG);
        if ("true".equals(debugSetting)) {
            LiquibaseLauncher.debug = true;
            debug("Debug mode enabled because either the JVM property 'liquibase.launcher.debug' or the environment " +
                "variable 'LIQUIBASE_LAUNCHER_DEBUG' is set to " + debugSetting);
        }

        String parentLoaderSetting = getSetting(LIQUIBASE_LAUNCHER_PARENT_CLASSLOADER);
        if (parentLoaderSetting == null) {
             parentLoaderSetting = "system";
        }
        debug("Liquibase launcher parent classloader is set to " + parentLoaderSetting);

        final String liquibaseHomeEnv = getSetting(LIQUIBASE_HOME);
        debug("Liquibase home: " + liquibaseHomeEnv);
        if (liquibaseHomeEnv == null || liquibaseHomeEnv.equals("")) {
            throw new IllegalArgumentException("Unable to find either 'liquibase.home' JVM property nor " +
                "'LIQUIBASE_HOME' environment variable");
        }
        File liquibaseHome = new File(liquibaseHomeEnv);

        List<URL> urls = new ArrayList<>();
        urls.add(new File(liquibaseHome, "internal/lib/liquibase-core.jar").toURI().toURL()); //make sure liquibase-core.jar is first in the list

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
                if (lib.getName().toLowerCase(Locale.US).endsWith(".jar") && !lib.getName().toLowerCase(Locale.US).equals("liquibase-core.jar")) {
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

        ClassLoader parentLoader;
        if (parentLoaderSetting.equalsIgnoreCase("system")) {
            //loading with the regular system classloader includes liquibase.jar in the parent.
            //That causes the parent classloader to load LiquibaseCommandLine which makes it not able to access files in the child classloader
            //The system classloader's parent is the boot classloader, which keeps the only classloader with liquibase-core.jar the same as the rest of the classes it needs to access.
            parentLoader = ClassLoader.getSystemClassLoader().getParent();

        } else if (parentLoaderSetting.equalsIgnoreCase("thread")) {
            parentLoader = Thread.currentThread().getContextClassLoader();
        } else {
            throw new RuntimeException("Unknown liquibase launcher parent classloader value: "+parentLoaderSetting);
        }

        final URLClassLoader classloader = new URLClassLoader(urls.toArray(new URL[0]), parentLoader);
        Thread.currentThread().setContextClassLoader(classloader);

        Class<?> cli = null;

        try {
            cli = classloader.loadClass(LiquibaseCommandLine.class.getName());
        } catch (ClassNotFoundException classNotFoundException) {
            throw new RuntimeException(
                String.format("Unable to find Liquibase classes in the configured home: '%s'.", liquibaseHome)
            );
        }

        cli.getMethod("main", String[].class).invoke(null, new Object[]{args});
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
