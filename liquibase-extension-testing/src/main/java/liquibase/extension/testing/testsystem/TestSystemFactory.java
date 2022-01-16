package liquibase.extension.testing.testsystem;

import liquibase.Scope;
import liquibase.configuration.ConfigurationValueConverter;
import liquibase.configuration.ConfiguredValue;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.plugin.AbstractPluginFactory;
import liquibase.util.StringUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        if (description.contains(":") || description.contains("?")) {
            final Definition definition = Definition.parse(description);

            final TestSystem plugin = super.getPlugin(definition.name);
            if (plugin == null) {
                throw new UnexpectedLiquibaseException("No test system: " + description);
            }
            plugin.setProfiles(definition.profiles);
            plugin.setProperties(definition.properties);

            return plugin;
        }

        final TestSystem plugin = super.getPlugin(description);
        if (plugin == null) {
            throw new UnexpectedLiquibaseException("No test system: " + description);
        }

        final ConfiguredValue<String> testSystemsValue = Scope.getCurrentScope().getSingleton(LiquibaseConfiguration.class).getCurrentConfiguredValue(ConfigurationValueConverter.STRING, null, "liquibase.sdk.testSystem.test");
        if (testSystemsValue != null) {
            for (String testConfig : StringUtil.splitAndTrim(testSystemsValue.getValue(), ",")) {
                final Definition definition = Definition.parse(testConfig);

                if (!definition.name.equals(plugin.getName())) {
                    continue;
                }

                plugin.setProfiles(definition.profiles);
                plugin.setProperties(definition.properties);
            }
        }


        return plugin;
    }

    private static class Definition {
        static final Pattern namePattern = Pattern.compile("^([^:?]+)");
        static final Pattern profilePattern = Pattern.compile(":([^?]*)");
        static final Pattern propertiesPattern = Pattern.compile("\\?(.*)");

        private String name;
        private String[] profiles;
        private String properties;

        private static Definition parse(String definition) {
            Definition returnObj = new Definition();

            final Matcher nameMatcher = namePattern.matcher(definition);
            if (!nameMatcher.find()) {
                throw new IllegalArgumentException("Cannot parse name from " + definition);
            }
            returnObj.name = nameMatcher.group(1);


            final Matcher profileMatcher = profilePattern.matcher(definition);
            if (profileMatcher.find()) {
                returnObj.profiles = StringUtil.splitAndTrim(profileMatcher.group(1), ",").toArray(new String[0]);
            }

            final Matcher propertiesMatcher = propertiesPattern.matcher(definition);
            if (propertiesMatcher.find()) {
                returnObj.properties = propertiesMatcher.group(1);
            }

            return returnObj;
        }
    }
}
