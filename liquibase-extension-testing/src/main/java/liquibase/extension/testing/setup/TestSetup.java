package liquibase.extension.testing.setup;

public abstract class TestSetup {

    public abstract void setup(TestSetupEnvironment testSetupEnvironment) throws Exception;
    public void cleanup() {

    }
}
