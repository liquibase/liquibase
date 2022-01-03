package liquibase.extension.testing.testsystem;

import liquibase.Scope;
import liquibase.configuration.ConfigurationValueConverter;
import liquibase.configuration.ConfiguredValue;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.plugin.Plugin;
import liquibase.util.StringUtil;
import org.junit.Assume;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.MultipleFailureException;
import org.junit.runners.model.Statement;
import org.spockframework.runtime.extension.IMethodInvocation;
import org.spockframework.runtime.model.ErrorInfo;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class TestSystem implements TestRule, Plugin {

    private static Set<String> testSystems = new HashSet<>();

    static {
        final ConfiguredValue<String> testSystemsValue = Scope.getCurrentScope().getSingleton(LiquibaseConfiguration.class).getCurrentConfiguredValue(ConfigurationValueConverter.STRING, null, "liquibase.sdk.testSystem.test");
        if (testSystemsValue != null) {
            testSystems.addAll(StringUtil.splitAndTrim(testSystemsValue.getValue(), ","));
        }
    }

    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                Assume.assumeTrue("Not running test against " + TestSystem.this.getDefinition() + ": liquibase.sdk.testSystem.test is " + StringUtil.join(testSystems, ", "), shouldTest());

                List<Throwable> errors = new ArrayList<Throwable>();

                try {
                    TestSystem.this.start(false);
                    base.evaluate();
//                    succeeded(description);
                } catch (Throwable e) {
                    errors.add(e);
//                    failed(e, description);
                } finally {
//                    if (TestEnvironment.this.isNewlyStarted()) {
//                        TestEnvironment.this.stop();
//                    }
//                    finished(description);
                }

                MultipleFailureException.assertEmpty(errors);
            }
        };
    }

    public boolean shouldTest() {
        return testSystems.contains(TestSystem.this.getDefinition());
    }

    public abstract String getDefinition();

    public <T> T getTestSystemProperty(String propertyName, Class<T> type) {
        return getTestSystemProperty(propertyName, type, false);
    }

    public <T> T getTestSystemProperty(String propertyName, Class<T> type, boolean required) {
        ConfigurationValueConverter<T> converter = null;
        if (type.equals(Class.class)) {
            converter = (ConfigurationValueConverter<T>) ConfigurationValueConverter.CLASS;
        } else if (type.equals(String.class)) {
            converter = (ConfigurationValueConverter<T>) ConfigurationValueConverter.STRING;
        }

        return getTestSystemProperty(propertyName, converter, required);
    }

    public <T> T getTestSystemProperty(String propertyName, ConfigurationValueConverter<T> converter, boolean required) {
        final LiquibaseConfiguration config = Scope.getCurrentScope().getSingleton(LiquibaseConfiguration.class);

        ConfiguredValue<T> configuredValue = config.getCurrentConfiguredValue(converter, null, "liquibase.sdk.testSystem." + getDefinition() + "." + propertyName);

        if (configuredValue.found()) {
            return configuredValue.getValue();
        }

        //fall back to "default" setup
        configuredValue = config.getCurrentConfiguredValue(converter, null, "liquibase.sdk.testSystem.default." + propertyName);
        if (configuredValue.found()) {
            return configuredValue.getValue();
        }

        if (required) {
            throw new UnexpectedLiquibaseException("No required liquibase.sdk.testSystem configuration for " + getDefinition() + " of " + propertyName + " set");
        }

        return null;
    }

    public abstract void start(boolean keepRunning) throws SQLException, Exception;

    public abstract void stop();

    public void beforeTest(IMethodInvocation invocation) {

    }

    public void afterTest(IMethodInvocation invocation, List<ErrorInfo> errors) {

    }

    public abstract int getPriority(String definition);
}
