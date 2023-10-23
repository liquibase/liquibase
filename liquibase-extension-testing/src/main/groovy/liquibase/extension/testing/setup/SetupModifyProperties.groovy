package liquibase.extension.testing.setup

import java.nio.file.Files

class SetupModifyProperties extends TestSetup {

    private final File propsFile
    private final String key
    private final String value

    SetupModifyProperties(File propsFile, String key, String value) {
        this.propsFile = propsFile
        this.key = key
        this.value = value
    }

    @Override
    void setup(TestSetupEnvironment testSetupEnvironment) throws Exception {
        Properties properties = new Properties()
        properties.load(Files.newInputStream(propsFile.toPath()))
        properties.put(key, value)
        properties.store(Files.newOutputStream(propsFile.toPath()), "Modified " + key)
    }
}
