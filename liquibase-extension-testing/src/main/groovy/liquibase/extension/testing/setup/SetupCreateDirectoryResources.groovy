package liquibase.extension.testing.setup

import liquibase.extension.testing.TestDatabaseConnections

class SetupCreateDirectoryResources extends TestSetup {

    private String directory

    SetupCreateDirectoryResources(String directory) {
        this.directory = directory
    }

    @Override
    void setup(TestDatabaseConnections.ConnectionStatus connectionStatus) throws Exception {
        File f = new File(directory)
        boolean b = f.mkdirs()
        if (! b) {
            if (! f.exists()) {
                throw new RuntimeException("Unable to create directory '" + directory + "'")
            }
        }
    }
}
