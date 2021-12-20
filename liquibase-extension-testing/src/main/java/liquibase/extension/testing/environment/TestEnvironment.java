package liquibase.extension.testing.environment;

import liquibase.Scope;
import liquibase.configuration.ConfigurationValueConverter;
import liquibase.configuration.ConfiguredValue;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.exception.UnexpectedLiquibaseException;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.MultipleFailureException;
import org.junit.runners.model.Statement;
import org.spockframework.runtime.extension.IMethodInvocation;
import org.spockframework.runtime.model.ErrorInfo;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public abstract class TestEnvironment implements TestRule {

    private final String env;

    public TestEnvironment(String env) {
        this.env = env;
    }

    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                List<Throwable> errors = new ArrayList<Throwable>();

                try {
                    TestEnvironment.this.start();
                    base.evaluate();
//                    succeeded(description);
                } catch (Throwable e) {
                    errors.add(e);
//                    failed(e, description);
                } finally {
//                    finished(description);
                }

                MultipleFailureException.assertEmpty(errors);
            }
        };
    }


    public String getEnv() {
        return env;
    }

    public abstract String getUsername();
    public abstract String getPassword();
    public abstract String getUrl();

    public static String getPropertyName(String env, String propertyName) {
        return "liquibase.sdk.env." + env + '.' + propertyName;
    }

    public static <T> T getProperty(String env, String propertyName, Class<T> type) {
        return getProperty(env, propertyName, type, false);
    }

    public static <T> T getProperty(String env, String propertyName, Class<T> type, boolean required) {
        ConfigurationValueConverter<T> converter = null;
        if (type.equals(Class.class)) {
            converter = (ConfigurationValueConverter<T>) ConfigurationValueConverter.CLASS;
        } else if (type.equals(String.class)) {
            converter = (ConfigurationValueConverter<T>) ConfigurationValueConverter.STRING;
        }

        return getProperty(env, propertyName, converter, required);
    }

    public static <T> T getProperty(String env, String propertyName, ConfigurationValueConverter<T> converter, boolean required) {
        final LiquibaseConfiguration config = Scope.getCurrentScope().getSingleton(LiquibaseConfiguration.class);

        final ConfiguredValue<T> configuredValue = config.getCurrentConfiguredValue(converter, null, getPropertyName(env, propertyName));

        if (configuredValue.found()) {
            return configuredValue.getValue();
        }

        if (required) {
            throw new UnexpectedLiquibaseException("No " + getPropertyName(env, propertyName) + " configured");
        }

        return null;
    }

    public abstract void start() throws SQLException, Exception;

    public abstract void stop();

    public abstract Connection openConnection() throws SQLException;

    public void beforeTest(IMethodInvocation invocation) {

    }

    public void afterTest(IMethodInvocation invocation, List<ErrorInfo> errors) {

    }

}
