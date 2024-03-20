package liquibase.integration.commandline;

import liquibase.Scope;
import liquibase.logging.mdc.customobjects.Version;
import liquibase.util.ObjectUtil;
import liquibase.util.StringUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import static liquibase.integration.commandline.LiquibaseLauncherSettings.LiquibaseLauncherSetting.LIQUIBASE_HOME;
import static liquibase.integration.commandline.LiquibaseLauncherSettings.getSetting;

public class VersionUtils {
    public static Path getLiquibaseHomePath(Path workingDirectory) throws IOException {
        return new File(ObjectUtil.defaultIfNull(getSetting(LIQUIBASE_HOME), workingDirectory.toAbsolutePath().toString())).getAbsoluteFile().getCanonicalFile().toPath();
    }

    public static List<String> listLibraries(Map<String, LibraryInfo> libraryInfo, Path liquibaseHomePath, Path workingDirectory, Version mdcVersion) throws IOException {
        List<Version.Library> mdcLibraries = new ArrayList<>(libraryInfo.size());
        List<String> libraries = new ArrayList<>(libraryInfo.size());
        for (LibraryInfo info : new TreeSet<>(libraryInfo.values())) {
            String filePath = info.file.getCanonicalPath();

            if (liquibaseHomePath != null && info.file.toPath().startsWith(liquibaseHomePath)) {
                filePath = liquibaseHomePath.relativize(info.file.toPath()).toString();
            }
            if (info.file.toPath().startsWith(workingDirectory)) {
                filePath = workingDirectory.relativize(info.file.toPath()).toString();
            }

            String libraryDescription = filePath + ":" +
                    " " + info.name +
                    " " + (info.version == null ? "UNKNOWN" : info.version) +
                    (info.vendor == null ? "" : " By " + info.vendor);
            libraries.add(libraryDescription);

            mdcLibraries.add(new Version.Library(info.name, filePath));
        }
        if (mdcVersion != null) {
            mdcVersion.setLiquibaseLibraries(new Version.LiquibaseLibraries(libraryInfo.size(), mdcLibraries));
        }
        return libraries;
    }

    public static Map<String, LibraryInfo> getLibraryInfoMap() throws URISyntaxException, IOException {
        Map<String, LibraryInfo> libraryInfo = new HashMap<>();
        final ClassLoader classLoader = VersionUtils.class.getClassLoader();
        if (classLoader instanceof URLClassLoader) {
            for (URL url : ((URLClassLoader) classLoader).getURLs()) {
                if (!url.toExternalForm().startsWith("file:")) {
                    continue;
                }
                final File file = new File(url.toURI());
                if (file.getName().equals("liquibase-core.jar")) {
                    continue;
                }
                if (file.exists() && file.getName().toLowerCase().endsWith(".jar")) {
                    final LibraryInfo thisInfo = getLibraryInfo(file);
                    libraryInfo.putIfAbsent(thisInfo.name, thisInfo);
                }
            }
        }
        return libraryInfo;
    }

    private static LibraryInfo getLibraryInfo(File pathEntryFile) throws IOException {
        try (final JarFile jarFile = new JarFile(pathEntryFile)) {
            final LibraryInfo libraryInfo = new LibraryInfo();
            libraryInfo.file = pathEntryFile;

            final Manifest manifest = jarFile.getManifest();
            if (manifest != null) {
                libraryInfo.name = getValue(manifest, "Bundle-Name", "Implementation-Title", "Specification-Title");
                libraryInfo.version = getValue(manifest, "Bundle-Version", "Implementation-Version", "Specification-Version");
                libraryInfo.vendor = getValue(manifest, "Bundle-Vendor", "Implementation-Vendor", "Specification-Vendor");
            }

            handleCompilerJarEdgeCase(pathEntryFile, jarFile, libraryInfo);

            if (libraryInfo.name == null) {
                libraryInfo.name = pathEntryFile.getName().replace(".jar", "");
            }
            return libraryInfo;
        }
    }

    /**
     * The compiler.jar file was accidentally added to the liquibase tar.gz distribution, and the compiler.jar
     * file does not contain a completed MANIFEST.MF file. This method loads the version out of the pom.xml
     * instead of using the manifest, only for the compiler.jar file.
     */
    private static void handleCompilerJarEdgeCase(File pathEntryFile, JarFile jarFile, LibraryInfo libraryInfo) {
        try {
            if (pathEntryFile.toString().endsWith("compiler.jar") && StringUtil.isEmpty(libraryInfo.version)) {
                ZipEntry entry = jarFile.getEntry("META-INF/maven/com.github.spullara.mustache.java/compiler/pom.properties");
                InputStream inputStream = jarFile.getInputStream(entry);

                Properties jarProperties = new Properties();
                jarProperties.load(inputStream);

                libraryInfo.version = jarProperties.getProperty("version");
            }
        } catch (Exception e) {
            Scope.getCurrentScope().getLog(VersionUtils.class).fine("Failed to load the version of compiler.jar from " +
                    "its pom.properties, this is relatively harmless, but could mean that the version of compiler.jar will " +
                    "not appear in the liquibase --version console output.", e);
        }
    }

    private static String getValue(Manifest manifest, String... keys) {
        for (String key : keys) {
            String value = manifest.getMainAttributes().getValue(key);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    public static class LibraryInfo implements Comparable<LibraryInfo> {
        public String vendor;
        public String name;
        public File file;
        public String version;

        @Override
        public int compareTo(LibraryInfo o) {
            return this.file.compareTo(o.file);
        }
    }
}
