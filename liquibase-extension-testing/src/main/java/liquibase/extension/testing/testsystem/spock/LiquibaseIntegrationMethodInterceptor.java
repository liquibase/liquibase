package liquibase.extension.testing.testsystem.spock;

import liquibase.extension.testing.testsystem.TestSystem;
import org.spockframework.runtime.extension.AbstractMethodInterceptor;
import org.spockframework.runtime.extension.IMethodInvocation;
import org.spockframework.runtime.model.FieldInfo;
import org.spockframework.runtime.model.SpecInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LiquibaseIntegrationMethodInterceptor extends AbstractMethodInterceptor {

    private final SpecInfo spec;
    private final LiquibaseIntegrationTestExtension.ErrorListener errorListener;

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
            TestSystem env = readContainerFromField(field, invocation);
            env.start(false);

            env.beforeTest(invocation);

        }
    }

    private void stopContainers(List<FieldInfo> containers, IMethodInvocation invocation) throws Exception {
        for (FieldInfo field : containers) {
            TestSystem env = readContainerFromField(field, invocation);

            // we assume first error is the one we want
            final Optional<Throwable> maybeException = Optional.ofNullable(errorListener.getErrors().get(0).getException());
            env.afterTest(invocation, errorListener.getErrors());

            env.stop();

        }
    }

    private static TestSystem readContainerFromField(FieldInfo f, IMethodInvocation invocation) {
        return (TestSystem) f.readValue(invocation.getInstance());
    }
}
