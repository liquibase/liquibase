package liquibase.extension.testing.setup

import liquibase.extension.testing.TestDatabaseConnections
import liquibase.util.FileUtil

class SetupCreateTempResources extends TestSetup {

    private String originalFile
    private String newFile
    private String baseDir

    SetupCreateTempResources(String originalFile, String newFile) {
        this(originalFile, newFile, "target/test-classes")
    }

    SetupCreateTempResources(String originalFile, String newFile, String baseDir) {
        this.originalFile = originalFile
        this.newFile = newFile
        this.baseDir = baseDir
    }

    @Override
    void setup(TestDatabaseConnections.ConnectionStatus connectionStatus) throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource(originalFile)
        File f = new File(url.toURI())
        String contents = FileUtil.getContents(f)
        File outputFile = new File(baseDir, newFile)
        FileUtil.write(contents, outputFile)
    }
}
