package liquibase.extension.testing.environment;

public class TestEnvironmentFactory {

    public static TestEnvironment getEnvironment(String env) {
        final Class envClass = TestEnvironment.getProperty(env, "envClass", Class.class, true);

        TestEnvironment testEnv = null;
        try {
            testEnv = (TestEnvironment) envClass.getConstructor(String.class).newInstance(env);
        } catch (Throwable e) {
            throw new RuntimeException("Cannot create "+envClass.getName()+": "+e.getMessage(), e);
        }

        return testEnv;
    }
}
