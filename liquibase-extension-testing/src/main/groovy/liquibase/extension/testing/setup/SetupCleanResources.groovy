package liquibase.extension.testing.setup


import liquibase.extension.testing.TestDatabaseConnections

import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path

class SetupCleanResources extends TestSetup {

    private final List<String> resourcesToDelete = new ArrayList<>()
    public enum CleanupMode { CLEAN_ON_SETUP, CLEAN_ON_CLEANUP, CLEAN_ON_BOTH}
    private CleanupMode cleanupMode

    SetupCleanResources(String[] resourcesToDelete) {
        this(CleanupMode.CLEAN_ON_CLEANUP, resourcesToDelete)
    }

    SetupCleanResources(CleanupMode cleanupMode, String[] resourcesToDelete) {
        this.cleanupMode = cleanupMode
        this.resourcesToDelete.addAll(resourcesToDelete as Set)
    }

    @Override
    void setup(TestDatabaseConnections.ConnectionStatus connectionStatus) throws Exception {
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
        for (String fileToDelete : resourcesToDelete) {
            File f = null
            URL url = Thread.currentThread().getContextClassLoader().getResource(fileToDelete)
            if (url == null) {
                f = new File(fileToDelete)
            } else {
                f = new File(url.toURI())
            }

            //
            // This will handle files and directories
            //
            if (f.exists()) {
                Path path = FileSystems.getDefault().getPath(f.getAbsolutePath());
                Files.walk(path)
                     .sorted(Comparator.reverseOrder())
                     .map({ p -> p.toFile() })
                     .forEach({ file -> file.delete() })
            }
        }
    }
}
