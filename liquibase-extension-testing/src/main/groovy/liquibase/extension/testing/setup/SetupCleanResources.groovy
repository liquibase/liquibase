package liquibase.extension.testing.setup

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class SetupCleanResources extends TestSetup {

    private final List<String> resourcesToDelete = new ArrayList<>()
    public enum CleanupMode { CLEAN_ON_SETUP, CLEAN_ON_CLEANUP, CLEAN_ON_BOTH}
    private CleanupMode cleanupMode
    private FilenameFilter filter
    private File resourceDirectory

    SetupCleanResources(String[] resourcesToDelete) {
        this(CleanupMode.CLEAN_ON_CLEANUP, resourcesToDelete)
    }

    SetupCleanResources(CleanupMode cleanupMode, String[] resourcesToDelete) {
        this.cleanupMode = cleanupMode
        this.resourcesToDelete.addAll(resourcesToDelete as Set)
    }

    SetupCleanResources(CleanupMode cleanupMode, FilenameFilter filter, File resourceDirectory) {
        this.cleanupMode = cleanupMode
        this.filter = filter
        this.resourceDirectory = resourceDirectory
    }

    SetupCleanResources(CleanupMode cleanupMode, File resourceDirectory) {
        this.cleanupMode = cleanupMode
        this.resourceDirectory = resourceDirectory
    }

    @Override
    void setup(TestSetupEnvironment testSetupEnvironment) throws Exception {
        if (cleanupMode == CleanupMode.CLEAN_ON_CLEANUP) {
            return
        }
        deleteFiles(resourcesToDelete)
    }

    @Override
    void cleanup() {
        if (cleanupMode == CleanupMode.CLEAN_ON_SETUP) {
            return
        }
        deleteFiles(resourcesToDelete)
    }

    private void deleteFiles(List<String> resourcesToDelete) {
        //
        // Delete resources in the specified directories that match the filter
        //
        if (filter != null) {
            deleteResourcesThatMatch()
            return
        } else if (resourceDirectory != null && resourceDirectory.isDirectory()) {
            deleteDirectory(resourceDirectory.toPath())
            return
        }

        for (String fileToDelete : resourcesToDelete) {
            File f = null
            URL url = Thread.currentThread().getContextClassLoader().getResource(fileToDelete)
            if (url == null) {
                f = new File(fileToDelete)
            } else {
                f = new File(url.toURI())
            }

            if (! f.exists()) {
                continue
            }
            if (f.isFile()) {
                f.delete()
            } else {
                f.deleteDir()
            }
        }
    }

    private static void deleteDirectory(Path directory) throws IOException {
        Files.walk(directory)
                .sorted(Comparator.reverseOrder()) // Sort in reverse order to delete contents before parent
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        System.err.println("Failed to delete: " + path + " - " + e.getMessage());
                    }
                });
    }

    void deleteResourcesThatMatch() {
        if (! resourceDirectory.isAbsolute()) {
            URL url = Thread.currentThread().getContextClassLoader().getResource(resourceDirectory.getName())
            if (url == null) {
                return;
            }
            resourceDirectory = new File(url.toURI())
        }
        assert resourceDirectory.isDirectory(): "The resource '$resourceName' is not a directory"
        String[] listOfFiles = resourceDirectory.list(filter)
        for (String s : listOfFiles) {
            File f = new File(resourceDirectory, s)
            if (f.exists()) {
                boolean b = f.delete()
                if (b) {
                    assert !f.exists(): "The file '$f' was not deleted"
                }
            }
        }
    }
}
