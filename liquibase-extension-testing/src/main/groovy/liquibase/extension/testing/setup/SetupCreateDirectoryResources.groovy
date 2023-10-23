package liquibase.extension.testing.setup

class SetupCreateDirectoryResources extends TestSetup {

    private String directory

    SetupCreateDirectoryResources(String directory) {
        this.directory = directory
    }

    @Override
    void setup(TestSetupEnvironment testSetupEnvironment) throws Exception {
        File f = new File(directory)
        boolean b = f.mkdirs()
        if (! b) {
            if (! f.exists()) {
                throw new RuntimeException("Unable to create directory '" + directory + "'")
            }
        }
    }
}
