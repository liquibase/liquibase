package liquibase.extension.testing.testsystem.spock;

import liquibase.Scope;
import liquibase.configuration.ConfigurationValueConverter;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.extension.testing.testsystem.TestSystem;
import liquibase.util.StringUtil;
import org.junit.Assume;
import org.spockframework.runtime.extension.AbstractMethodInterceptor;
import org.spockframework.runtime.extension.IMethodInvocation;
import org.spockframework.runtime.model.FieldInfo;
import org.spockframework.runtime.model.SpecInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

public class LiquibaseIntegrationMethodInterceptor extends AbstractMethodInterceptor {

    private static final SortedSet<TestSystem.Definition> testSystems = new TreeSet<>();

    private final SpecInfo spec;
    private final LiquibaseIntegrationTestExtension.ErrorListener errorListener;

    private static final String configuredTestSystems;

    static {
        //cache configured test systems for faster lookup
        configuredTestSystems = Scope.getCurrentScope().getSingleton(LiquibaseConfiguration.class).getCurrentConfiguredValue(ConfigurationValueConverter.STRING, null, "liquibase.sdk.testSystem.test").getValue();
        if (configuredTestSystems != null) {
            for (String definition : StringUtil.splitAndTrim(configuredTestSystems, ","))
                testSystems.add(TestSystem.Definition.parse(definition));
        }
    }

    LiquibaseIntegrationMethodInterceptor(SpecInfo spec, LiquibaseIntegrationTestExtension.ErrorListener errorListener) {
        this.spec = spec;
        this.errorListener = errorListener;
    }

    @Override
    public void interceptSetupSpecMethod(IMethodInvocation invocation) throws Throwable {
        final List<FieldInfo> containers = findAllContainers();
        startContainers(containers, invocation);

        invocation.proceed();
    }

    @Override
    public void interceptCleanupSpecMethod(IMethodInvocation invocation) throws Throwable {
        final List<FieldInfo> containers = findAllContainers();
        stopContainers(containers, invocation);

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

            Assume.assumeTrue("Not running test against " + testSystem.getDefinition() + ": liquibase.sdk.testSystem.test is " + configuredTestSystems, testSystem.shouldTest());

            testSystem.start();

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
}
