package liquibase.extension.testing.testsystem.spock;

import liquibase.extension.testing.testsystem.TestSystem;
import org.spockframework.runtime.extension.IGlobalExtension;
import org.spockframework.runtime.model.SpecInfo;

public class LiquibaseGlobalExtension implements IGlobalExtension {
    @Override
    public void start() {

    }

    @Override
    public void visitSpec(SpecInfo spec) {

    }

    @Override
    public void stop() {
        for (TestSystem startedTestSystem : LiquibaseIntegrationMethodInterceptor.startedTestSystems) {
            try {
                startedTestSystem.stop();
                LiquibaseIntegrationMethodInterceptor.startedTestSystems.remove(startedTestSystem);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
