package liquibase.extension.testing.testsystem.spock;

import liquibase.Scope;
import liquibase.configuration.ConfigurationValueConverter;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.extension.testing.testsystem.DatabaseTestSystem;
import liquibase.extension.testing.testsystem.TestSystem;
import org.junit.Assume;
import org.spockframework.runtime.extension.AbstractMethodInterceptor;
import org.spockframework.runtime.extension.IMethodInvocation;
import org.spockframework.runtime.model.FieldInfo;
import org.spockframework.runtime.model.SpecInfo;

import java.util.*;

public class LiquibaseIntegrationMethodInterceptor extends AbstractMethodInterceptor {

    private static final SortedSet<TestSystem.Definition> testSystems = new TreeSet<>();
    public static final Set<TestSystem> startedTestSystems = new HashSet<>();

    private final SpecInfo spec;
    private final LiquibaseIntegrationTestExtension.ErrorListener errorListener;

    private static final String configuredTestSystems;
    private static final String skippedTestSystems;

    static {
        configuredTestSystems = Scope.getCurrentScope().getSingleton(LiquibaseConfiguration.class).getCurrentConfiguredValue(ConfigurationValueConverter.STRING, null, "liquibase.sdk.testSystem.test").getValue();
        skippedTestSystems = Scope.getCurrentScope().getSingleton(LiquibaseConfiguration.class).getCurrentConfiguredValue(ConfigurationValueConverter.STRING, null, "liquibase.sdk.testSystem.skip").getValue();

        for (String definition : TestSystem.getEnabledTestSystems(configuredTestSystems, skippedTestSystems)) {
            testSystems.add(TestSystem.Definition.parse(definition));
        }
    }

    LiquibaseIntegrationMethodInterceptor(SpecInfo spec, LiquibaseIntegrationTestExtension.ErrorListener errorListener) {
        verifyNoMultipleDatabases(spec);
        this.spec = spec;
        this.errorListener = errorListener;
    }

    private void verifyNoMultipleDatabases(SpecInfo spec) {
        List<FieldInfo> allFields = spec.getAllFields();
        int databases = 0;
        for (FieldInfo field : allFields) {
            if (field.getType() != Object.class && field.getType().isAssignableFrom(DatabaseTestSystem.class)) {
                databases++;
            }
        }
        if (databases > 1) {
            throw new UnexpectedLiquibaseException(spec.getName() + " defines more than one " + DatabaseTestSystem.class.getSimpleName() + ". This is not permitted because the test will not be run in any of the matrices on GitHub Actions. You'll need to make a separate class for each of the databases.");
        }
    }

    @Override
    public void interceptSetupSpecMethod(IMethodInvocation invocation) throws Throwable {
        final List<FieldInfo> containers = findAllContainers();
        startContainers(containers, invocation);

        invocation.proceed();
    }

    private List<FieldInfo> findAllContainers() {
        List<FieldInfo> returnList = new ArrayList<>();
        for (FieldInfo fieldInfo : spec.getAllFields()) {
            if (TestSystem.class.isAssignableFrom(fieldInfo.getType())) {
                assert fieldInfo.isShared() : "TestEnvironment field " + fieldInfo.getName() + " must be @Shared";
                returnList.add(fieldInfo);
            }
        }
        return returnList;
    }

    private static void startContainers(List<FieldInfo> containers, IMethodInvocation invocation) throws Exception {
        for (FieldInfo field : containers) {
            TestSystem testSystem = readContainerFromField(field, invocation);

            Assume.assumeTrue("Not running test against " + testSystem.getDefinition() + ": liquibase.sdk.testSystem.test is " + configuredTestSystems + " and liquibase.sdk.testSystem.skip is " + skippedTestSystems, testSystem.shouldTest());

            testSystem.start();
            startedTestSystems.add(testSystem);

        }
    }

    private void stopContainers(List<FieldInfo> containers, IMethodInvocation invocation) throws Exception {
        for (FieldInfo field : containers) {
            TestSystem testSystem = readContainerFromField(field, invocation);

            if (!testSystem.shouldTest()) {
                continue;
            }

            try {
                testSystem.stop();
            } catch (Exception e) {
                Scope.getCurrentScope().getLog(getClass()).warning("Cannot stop "+testSystem.getDefinition());
            }

        }
    }

    private static TestSystem readContainerFromField(FieldInfo f, IMethodInvocation invocation) {
        return (TestSystem) f.readValue(invocation.getInstance());
    }

    /**
     * Required for executing Spock cleanupSpec fixture method.
     *
     * @param invocation the cleanup method invocation
     */
    @Override
    public void interceptCleanupSpecMethod(IMethodInvocation invocation) throws Throwable {
        invocation.proceed();
    }
}
