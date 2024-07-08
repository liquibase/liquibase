package liquibase.extension.testing.setup


import liquibase.util.FileUtil

class SetupCreateTempResources extends TestSetup {

    private String originalFile
    private String newFile
    private String baseDir
    private Date lastModified

    SetupCreateTempResources(String originalFile, String newFile) {
        this(originalFile, newFile, "target/test-classes")
    }

    SetupCreateTempResources(String originalFile, String newFile, String baseDir) {
        this(originalFile, newFile, baseDir, null)
    }

    SetupCreateTempResources(String originalFile, String newFile, String baseDir, Date lastModified) {
        this.originalFile = originalFile
        this.newFile = newFile
        this.baseDir = baseDir
        this.lastModified = lastModified
    }

    @Override
    void setup(TestSetupEnvironment testSetupEnvironment) throws Exception {
        try {
            URL url = Thread.currentThread().getContextClassLoader().getResource(originalFile)
            File f = new File(url.toURI())
            String contents = FileUtil.getContents(f)
            File outputFile = new File(baseDir, newFile)
            FileUtil.write(contents, outputFile)
            if (lastModified != null) {
                outputFile.setLastModified(lastModified.getTime())
            }
        } catch (Exception e) {
            throw new Exception("Failed to copy resource " + originalFile + " during test setup.", e)
        }
    }
}
