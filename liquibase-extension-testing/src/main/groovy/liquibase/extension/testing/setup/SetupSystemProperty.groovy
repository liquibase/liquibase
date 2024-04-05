package liquibase.extension.testing.setup

/**
 * Adds a Java System Property, which are accessed via {@link System#getProperty}.
 */
class SetupSystemProperty extends TestSetup {

    private final String key
    private final String value
    private String existingValue

    SetupSystemProperty(String key, String value) {
        this.key = key
        this.value = value
    }

    @Override
    void setup(TestSetupEnvironment testSetupEnvironment) throws Exception {
        existingValue = System.setProperty(key, value)
    }

    @Override
    void cleanup() {
        if (existingValue == null) {
            System.clearProperty(key)
        } else {
            System.setProperty(key, existingValue)
        }
    }
}
