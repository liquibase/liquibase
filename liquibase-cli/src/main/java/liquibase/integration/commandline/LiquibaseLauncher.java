package liquibase.integration.commandline;

import liquibase.Scope;
import liquibase.util.StringUtil;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static liquibase.integration.commandline.LiquibaseLauncherSettings.LiquibaseLauncherSetting.*;
import static liquibase.integration.commandline.LiquibaseLauncherSettings.getSetting;

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

    private static final String LIQUIBASE_CORE_JAR_PATTERN = ".*?/liquibase-core([-0-9.])*.jar";
    private static final String LIQUIBASE_COMMERCIAL_JAR_PATTERN = ".*?/liquibase-commercial([-0-9.])*.jar";
    private static final String LIQUIBASE_CORE_MESSAGE = "Liquibase Core";
    private static final String LIQUIBASE_COMMERCIAL_MESSAGE = "Liquibase Commercial";
    private static final String DEPENDENCY_JAR_VERSION_PATTERN = "(.*?)-?[0-9.]*.jar";
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

        List<URL> libUrls = getLibUrls(liquibaseHome);
        checkForDuplicatedJars(libUrls);

        if (debug) {
            debug("Final Classpath:");
            for (URL url : libUrls) {
                debug("  " + url.toString());
            }
        }

        ClassLoader parentLoader = getClassLoader(parentLoaderSetting);

        final URLClassLoader classloader = new URLClassLoader(libUrls.toArray(new URL[0]), parentLoader);
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

    private static ClassLoader getClassLoader(String parentLoaderSetting) {
        if (parentLoaderSetting.equalsIgnoreCase("system")) {
            //loading with the regular system classloader includes liquibase.jar in the parent.
            //That causes the parent classloader to load LiquibaseCommandLine which makes it not able to access files in the child classloader
            //The system classloader's parent is the boot classloader, which keeps the only classloader with liquibase-core.jar the same as the rest of the classes it needs to access.
            return ClassLoader.getSystemClassLoader().getParent();

        } else if (parentLoaderSetting.equalsIgnoreCase("thread")) {
            return Thread.currentThread().getContextClassLoader();
        } else {
            throw new RuntimeException("Unknown liquibase launcher parent classloader value: "+ parentLoaderSetting);
        }
    }

    /**
     * Check for duplicate core and commercial JAR files
     */
    private static void checkForDuplicatedJars(List<URL> urls) {
        List<String> duplicateCore =
                urls
                  .stream()
                  .map(URL::getFile)
                  .filter(file -> file.matches(LIQUIBASE_CORE_JAR_PATTERN))
                  .collect(Collectors.toList());
        List<String> duplicateCommercial =
                urls
                  .stream()
                  .map(URL::getFile)
                  .filter(file -> file.matches(LIQUIBASE_COMMERCIAL_JAR_PATTERN))
                  .collect(Collectors.toList());
        if (duplicateCore.size() > 1) {
            buildDupsMessage(duplicateCore, LIQUIBASE_CORE_MESSAGE);
        }
        if (duplicateCommercial.size() > 1) {
            buildDupsMessage(duplicateCommercial, LIQUIBASE_COMMERCIAL_MESSAGE);
        }
        Map<String, List<String>> duplicates = new LinkedHashMap<>();
        findVersionedDuplicates(urls, Pattern.compile(DEPENDENCY_JAR_VERSION_PATTERN), duplicates);
        findExactDuplicates(urls, duplicates);
        duplicates.forEach((key, value) -> {
            if (value.size() > 1) {
                buildDupsMessage(value, key);
            }
        });
    }

    private static List<URL> getLibUrls(File liquibaseHome) throws MalformedURLException {
        List<URL> urls = new ArrayList<>();
        urls.add(new File(liquibaseHome, "internal/lib/liquibase-core.jar").toURI().toURL()); //make sure liquibase-core.jar is first in the list

        File[] libDirs = new File[]{
                new File("./liquibase_libs"),
                new File(liquibaseHome, "lib"),
                new File(liquibaseHome, "internal/lib"),
                new File(liquibaseHome, "internal/extensions"),
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
        return urls;
    }

    //
    // Find duplicates that match pattern <name>-<version>, like snakeyaml-1.33.0 and snakeyaml-2.0
    //
    private static void findVersionedDuplicates(List<URL> urls, Pattern versionedJarPattern, Map<String, List<String>> duplicates) {
        urls
          .forEach(url -> {
              Matcher m = versionedJarPattern.matcher(new File(url.getFile()).getName());
              if (m.find()) {
                  String key = m.group(1);
                  List<String> dupEntries = duplicates.get(key);
                  if (dupEntries == null) {
                      dupEntries = new ArrayList<>();
                  }
                  dupEntries.add(url.getFile());
                  duplicates.put(key, dupEntries);
              }
          });
    }

    //
    // Find exact duplicates in the list of URL
    // skip core and commercial JARs and any directories
    //
    private static void findExactDuplicates(List<URL> urls, Map<String, List<String>> duplicates) {
        urls
            .forEach(url -> {
                String key = new File(url.getFile()).getName();
                if (! (url.toString().endsWith("/") || url.toString().endsWith("\\") ||
                       key.contains("liquibase-core") || key.contains("liquibase-commercial"))) {
                    List<String> dupEntries = duplicates.get(key);
                    if (dupEntries == null) {
                        dupEntries = new ArrayList<>();
                    }
                    dupEntries.add(url.getFile());
                    duplicates.put(key, dupEntries);
                }
            });
    }

    private static void buildDupsMessage(List<String> duplicates, String title) {
        String jarString = StringUtil.join(duplicates, System.lineSeparator());
        String message = String.format("*** Duplicate %s JAR files ***%n%s", title, jarString);
        Scope.getCurrentScope().getUI().sendMessage(String.format("WARNING: %s", message));
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
