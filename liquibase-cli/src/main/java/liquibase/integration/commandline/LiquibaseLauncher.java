package liquibase.integration.commandline;

import liquibase.Scope;
import liquibase.util.StringUtil;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Launcher which builds up the classpath needed to run Liquibase, then calls {@link LiquibaseCommandLine#main(String[])}.
 * <p>
 * It looks for a LIQUIBASE_LAUNCHER_DEBUG env variable to determine if it should log what it is doing to stderr.
 */
public class LiquibaseLauncher {

    private static final String LIQUIBASE_CORE_JAR_PATTERN = ".*?/liquibase-core([-0-9.])*.jar";
    private static final String LIQUIBASE_COMMERCIAL_JAR_PATTERN = ".*?/liquibase-commercial([-0-9.])*.jar";
    private static final String LIQUIBASE_CORE_MESSAGE = "Liquibase Core";
    private static final String LIQUIBASE_COMMERCIAL_MESSAGE = "Liquibase Commercial";
    private static final String DEPENDENCY_JAR_VERSION_PATTERN = "(.*?)-([0-9.]*).jar";
    private static boolean debug = false;

    public static void main(final String[] args) throws Exception {

        final String debugSetting = System.getenv("LIQUIBASE_LAUNCHER_DEBUG");
        if (debugSetting != null && debugSetting.equals("true")) {
            LiquibaseLauncher.debug = true;
            debug("Debug mode enabled because LIQUIBASE_LAUNCHER_DEBUG is set to " + debugSetting);
        }

        String parentLoaderSetting = System.getenv("LIQUIBASE_LAUNCHER_PARENT_CLASSLOADER");
        if (parentLoaderSetting == null) {
             parentLoaderSetting = "system";
        }
        debug("LIQUIBASE_LAUNCHER_PARENT_CLASSLOADER is set to " + parentLoaderSetting);

        final String liquibaseHomeEnv = System.getenv("LIQUIBASE_HOME");
        debug("LIQUIBASE_HOME: " + liquibaseHomeEnv);
        if (liquibaseHomeEnv == null || liquibaseHomeEnv.equals("")) {
            throw new IllegalArgumentException("Unable to find LIQUIBASE_HOME environment variable");
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

        //
        // Check for duplicate core and commercial JAR files
        //

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
            throw new RuntimeException("Unknown LIQUIBASE_LAUNCHER_PARENT_CLASSLOADER value: "+parentLoaderSetting);
        }

        final URLClassLoader classloader = new URLClassLoader(urls.toArray(new URL[0]), parentLoader);
        Thread.currentThread().setContextClassLoader(classloader);

        final Class<?> cli = classloader.loadClass(LiquibaseCommandLine.class.getName());

        cli.getMethod("main", String[].class).invoke(null, new Object[]{args});
    }

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
