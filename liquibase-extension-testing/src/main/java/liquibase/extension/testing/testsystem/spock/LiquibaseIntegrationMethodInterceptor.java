package liquibase.extension.testing.testsystem.spock;

import liquibase.Scope;
import liquibase.command.CommandScope;
import liquibase.command.core.DropAllCommandStep;
import liquibase.command.core.helpers.DbUrlConnectionArgumentsCommandStep;
import liquibase.configuration.ConfigurationValueConverter;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.extension.testing.testsystem.DatabaseTestSystem;
import liquibase.extension.testing.testsystem.TestSystem;
import liquibase.lockservice.LockService;
import liquibase.lockservice.LockServiceFactory;
import liquibase.structure.core.DatabaseObjectFactory;
import org.junit.jupiter.api.Assumptions;
import org.spockframework.runtime.extension.AbstractMethodInterceptor;
import org.spockframework.runtime.extension.IMethodInvocation;
import org.spockframework.runtime.model.FieldInfo;
import org.spockframework.runtime.model.SpecInfo;

import java.io.ByteArrayOutputStream;
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
        dropAllDatabases(invocation);
        invocation.proceed();
    }

    @Override
    public void interceptSetupMethod(IMethodInvocation invocation) throws Throwable {
        dropAllDatabases(invocation);
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

            Assumptions.assumeTrue(testSystem.shouldTest(), "Not running test against " + testSystem.getDefinition() + ": liquibase.sdk.testSystem.test is " + configuredTestSystems + " and liquibase.sdk.testSystem.skip is " + skippedTestSystems);

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
        TestSystem testSystem = (TestSystem) f.readValue(invocation.getInstance());
        if (testSystem == null) {
            testSystem = (TestSystem) f.readValue(invocation.getSharedInstance());
        }
        return testSystem;
    }

    /**
     * Required for executing Spock cleanupSpec fixture method.
     *
     * @param invocation the cleanup method invocation
     */
    @Override
    public void interceptCleanupSpecMethod(IMethodInvocation invocation) throws Throwable {
        dropAllDatabases(invocation);
        invocation.proceed();
    }

    private void dropAllDatabases(IMethodInvocation invocation) throws Exception {
        for (TestSystem startedTestSystem : startedTestSystems) {
            if (startedTestSystem instanceof DatabaseTestSystem) {
                // Only drop the database if it was used in this test.
                final List<FieldInfo> containers = findAllContainers();
                for (FieldInfo field : containers) {
                    TestSystem testSystem = readContainerFromField(field, invocation);
                    if (testSystem == startedTestSystem) {
                        runDropAll(((DatabaseTestSystem) startedTestSystem));
                    }
                }
            }
        }
        // Start tests from a clean slate, otherwise the MDC will be polluted with info about the dropAll command.
        Scope.getCurrentScope().getMdcManager().clear();
        DatabaseObjectFactory.getInstance().reset();
    }

    @Override
    public void interceptCleanupMethod(IMethodInvocation invocation) throws Throwable {
        dropAllDatabases(invocation);
        invocation.proceed();
    }

    private static void runDropAll(DatabaseTestSystem db) throws Exception {
        LockService lockService = LockServiceFactory.getInstance().getLockService(db.getDatabaseFromFactory());
        lockService.releaseLock();
        CommandScope commandScope = new CommandScope(DropAllCommandStep.COMMAND_NAME);
        commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, db.getConnectionUrl());
        commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.USERNAME_ARG, db.getUsername());
        commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.PASSWORD_ARG, db.getPassword());
        // this is a pro only argument, but is added here because there is no mechanism for adding the argument from the pro tests
        commandScope.addArgumentValue("dropDbclhistory", true);
        commandScope.setOutput(new ByteArrayOutputStream());
        commandScope.execute();
    }
}
