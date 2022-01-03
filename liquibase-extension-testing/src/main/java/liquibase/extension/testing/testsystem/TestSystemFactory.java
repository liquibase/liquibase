package liquibase.extension.testing.testsystem;

import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.plugin.AbstractPluginFactory;

import java.util.HashMap;
import java.util.Map;

public class TestSystemFactory extends AbstractPluginFactory<TestSystem> {

    @Override
    protected Class<TestSystem> getPluginClass() {
        return TestSystem.class;
    }

    @Override
    protected int getPriority(TestSystem testSystem, Object... args) {
        return testSystem.getPriority((String) args[0]);
    }

    public TestSystem getTestSystem(String description) {
        final TestSystem plugin = super.getPlugin(description);
        if (plugin == null) {
            throw new UnexpectedLiquibaseException("No test system: "+description);
        }
        return plugin;
    }
}
