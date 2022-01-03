package liquibase.extension.testing.setup


import liquibase.util.FileUtil

class SetupCreateTempResources extends TestSetup {

    private String originalFile
    private String newFile

    SetupCreateTempResources(String originalFile, String newFile) {
        this.originalFile = originalFile
        this.newFile = newFile
    }

    @Override
    void setup(TestSetupEnvironment testSetupEnvironment) throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource(originalFile)
        File f = new File(url.toURI())
        String contents = FileUtil.getContents(f)
        File outputFile = new File("target/test-classes", newFile)
        FileUtil.write(contents, outputFile)
    }
}
