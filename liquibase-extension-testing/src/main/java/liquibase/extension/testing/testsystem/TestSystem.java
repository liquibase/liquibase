package liquibase.extension.testing.testsystem;

import liquibase.Scope;
import liquibase.configuration.ConfigurationValueConverter;
import liquibase.configuration.ConfiguredValue;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.plugin.Plugin;
import liquibase.servicelocator.ServiceLocator;
import liquibase.util.CollectionUtil;
import liquibase.util.StringUtil;
import liquibase.util.SystemUtil;
import org.junit.Assume;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.MultipleFailureException;
import org.junit.runners.model.Statement;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * TestSystem implementations define and manage a connection to an external system to test.
 * Ideally the implementation can start and stop the test systems, but that is not necessary.
 * <br><br>
 * This implements {@link TestRule} so it can control start/stop of the TestSystem in JUnit, but that may be removed as tests get converted to spock.
 * <br><br>
 * Instances should not be created directly, but via {@link TestSystemFactory}
 */
public abstract class TestSystem implements TestRule, Plugin {

    private static final SortedSet<TestSystem.Definition> testSystems = new TreeSet<>();
    private static final String configuredTestSystems;
    private static final String skippedTestSystems;
    private static final Pattern COMPILE = Pattern.compile("(\\$\\{.+?})");

    private final Definition definition;

    private final SortedSet<String> configurationKeys = new TreeSet<>(Collections.singletonList(
            "keepRunning"
    ));

    static {
        configuredTestSystems = Scope.getCurrentScope().getSingleton(LiquibaseConfiguration.class).getCurrentConfiguredValue(ConfigurationValueConverter.STRING, null, "liquibase.sdk.testSystem.test").getValue();
        skippedTestSystems = Scope.getCurrentScope().getSingleton(LiquibaseConfiguration.class).getCurrentConfiguredValue(ConfigurationValueConverter.STRING, null, "liquibase.sdk.testSystem.skip").getValue();

        for (String definition : getEnabledTestSystems(configuredTestSystems, skippedTestSystems)) {
            testSystems.add(TestSystem.Definition.parse(definition));
        }
    }

    /**
     * Determine which test systems are considered enabled and should have tests run against them.
     * @param configuredTestSystems the value of the "liquibase.sdk.testSystem.test" property
     * @param skippedTestSystems the value of the "liquibase.sdk.testSystem.skip" property
     * @return the list of test system names that are enabled
     */
    public static List<String> getEnabledTestSystems(String configuredTestSystems, String skippedTestSystems) {
        List<String> returnList;

        if (StringUtil.isNotEmpty(configuredTestSystems) && configuredTestSystems.equals("all")) {
            ServiceLocator serviceLocator = Scope.getCurrentScope().getServiceLocator();
            returnList = serviceLocator.findInstances(TestSystem.class).stream()
                    .map(testSystem -> testSystem.getDefinition().getName()).distinct().collect(Collectors.toList());
        } else {
            returnList = CollectionUtil.createIfNull(StringUtil.splitAndTrim(configuredTestSystems, ","));
        }

        if (StringUtil.isNotEmpty(skippedTestSystems)) {
            List<String> skippedTestSystemsList = CollectionUtil.createIfNull(StringUtil.splitAndTrim(skippedTestSystems, ","));
            returnList = returnList.stream().filter(ts -> !skippedTestSystemsList.contains(ts)).collect(Collectors.toList());
        }

        if (!SystemUtil.isAtLeastJava11()) {
            returnList = returnList.stream().filter(ts -> !"hsqldb".equals(ts)).collect(Collectors.toList());
        }
        return returnList;
    }

    /**
     * Empty constructor for ServiceLocator to use
     */
    protected TestSystem(String name) {
        this(new Definition(name));
    }

    /**
     * Constructor for {@link TestSystemFactory} to use
     */
    protected TestSystem(Definition definition) {
        this.definition = definition;
    }

    /**
     * Return configuration keys supported by this testSystem
     */
    public SortedSet<String> getConfigurationKeys() {
        return configurationKeys;
    }

    /**
     * Allows test system to be auto-controlled by JUnit tests.
     * <p>
     * If the liquibase.sdk.testSystem.test configuration does NOT include the name of this test system, it <b>skips</b> the test.<br>
     * If the liquibase.sdk.testSystem.test configuration DOES include the name of the test system, it will connect to a matching system if available or will start it as possible.
     * If it starts the system, it will not stop it until JVM shutdown.
     * <p>
     * Example:
     * <pre>
     * \@Rule
     * TestSystem testSystem = Scope.getCurrentScope().getSingleton(TestSystemFactory.class).getTestSystem("mysql")
     * </pre>
     */
    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                Assume.assumeTrue("Not running test against " + TestSystem.this.getDefinition() + ": liquibase.sdk.testSystem.test is " + configuredTestSystems + " and liquibase.sdk.testSystem.skip is " + skippedTestSystems, shouldTest());

                List<Throwable> errors = new ArrayList<>();

                try {
                    TestSystem.this.start();
                    base.evaluate();
                } catch (Throwable e) {
                    errors.add(e);
                }

                MultipleFailureException.assertEmpty(errors);
            }
        };
    }

    /**
     * Default implementation returns PRIORITY_DEFAULT if the name matches the given definition, without taking any profiles etc. into account.
     */
    public int getPriority(Definition definition) {
        if (definition.getName().equals(getDefinition().getName())) {
            return PRIORITY_DEFAULT;
        }

        return PRIORITY_NOT_APPLICABLE;
    }

    /**
     * @return true if this TestSystem should have automated tests run against it
     */
    public boolean shouldTest() {
        return testSystems.contains(TestSystem.this.getDefinition());
    }

    /**
     * Return the definition of this test system.
     */
    public Definition getDefinition() {
        return definition;
    }

    /**
     * Return whether this testSystem should/will keep running after the JVM interacting with it exits.
     * Default implementation returns the `keepRunning` test system configured value.
     */
    public boolean getKeepRunning() {
        return getConfiguredValue("keepRunning", Boolean.class);
    }

    /**
     * Convenience method for {@link #getConfiguredValue(String, ConfigurationValueConverter, boolean)}
     */
    public <T> T getConfiguredValue(String propertyName, Class<T> type) {
        return getConfiguredValue(propertyName, type, false);
    }

    /**
     * Convenience method for {@link #getConfiguredValue(String, ConfigurationValueConverter, boolean)}
     */
    public <T> T getConfiguredValue(String propertyName, Class<T> type, boolean required) {
        ConfigurationValueConverter<T> converter = null;
        if (type.equals(Class.class)) {
            converter = (ConfigurationValueConverter<T>) ConfigurationValueConverter.CLASS;
        } else if (type.equals(String.class)) {
            converter = (ConfigurationValueConverter<T>) ConfigurationValueConverter.STRING;
        }

        return getConfiguredValue(propertyName, converter, required);
    }

    /**
     * Returns the configured value for the given propertyName. It will check (in priority order):
     * <ol>
     * <li>properties set directly on this object</li>
     * <li>liquibase.sdk.testSystem.[name].[profile(s)].propertyName in the order the profiles are set on this object</li>
     * <li>liquibase.sdk.testSystem.[name].propertyName</li>
     * <li>liquibase.sdk.testSystem.default.propertyName</li>
     * </ol>
     * <br>
     * If a value is not found, it will return null or throw an {@link UnexpectedLiquibaseException} if 'required' is true.
     */
    public <T> T getConfiguredValue(String propertyName, ConfigurationValueConverter<T> converter, boolean required) {
        ConfigurationValueConverter<T> finalConverter = value -> {
            if (value instanceof String && ((String) value).contains("${")) {
                final Matcher matcher = COMPILE.matcher((String) value);
                while (matcher.find()) {
                    final String config = matcher.group(1).replace("${", "").replace("}", "").trim();
                    value = ((String) value).replace(matcher.group(1), getConfiguredValue(config, String.class));
                }
            }

            if (converter == null) {
                return (T) value;
            } else {
                return converter.convert(value);
            }
        };

        final SortedMap<String, Object> properties = definition.getProperties();
        if (properties.containsKey(propertyName)) {
            return finalConverter.convert(properties.get(propertyName));
        }

        final LiquibaseConfiguration config = Scope.getCurrentScope().getSingleton(LiquibaseConfiguration.class);

        ConfiguredValue<T> configuredValue;
        //first check profiles
        for (String profile : definition.getProfiles()) {
            configuredValue = config.getCurrentConfiguredValue(finalConverter, null, "liquibase.sdk.testSystem." + getDefinition().getName() + ".profiles." + profile + "." + propertyName);

            if (configuredValue.found()) {
                return configuredValue.getValue();
            }
        }

        configuredValue = config.getCurrentConfiguredValue(finalConverter, null, "liquibase.sdk.testSystem." + getDefinition().getName() + "." + propertyName);

        if (configuredValue.found()) {
            return configuredValue.getValue();
        }

        //fall back to "default" setup
        configuredValue = config.getCurrentConfiguredValue(finalConverter, null, "liquibase.sdk.testSystem.default." + propertyName);
        if (configuredValue.found()) {
            return configuredValue.getValue();
        }

        if (required) {
            throw new UnexpectedLiquibaseException("No required liquibase.sdk.testSystem configuration for " + getDefinition().getName() + " of " + propertyName + " set");
        }

        return null;
    }

    /**
     * Starts the system if possible.
     * Does not return until test system is reachable.
     * If connecting to a running system, ensure the system can be reached.
     * The lifetime of the started system should respect the {@link #getKeepRunning()} configuration.
     * If the keepRunning flag has an invalid value for this test system, throw an {@link IllegalArgumentException}.
     *
     * @throws Exception if the system cannot be started or reached.
     */
    public abstract void start() throws Exception;

    /**
     * Stops the system if possible.
     * Does not return until test system is down.
     */
    public abstract void stop() throws Exception;

    @Override
    public String toString() {
        return definition.toString();
    }

    public static class Definition implements Comparable<Definition> {
        private static final Pattern namePattern = Pattern.compile("^([^:?]+)");
        private static final Pattern profilePattern = Pattern.compile(":([^?]*)");
        private static final Pattern propertiesPattern = Pattern.compile("\\?(.*)");

        private final String name;
        private final String[] profiles;
        private final SortedMap<String, Object> properties = new TreeMap<>();

        public static Definition parse(String definition) {
            if (definition == null) {
                return null;
            }

            String name;

            final Matcher nameMatcher = namePattern.matcher(definition);
            if (!nameMatcher.find()) {
                throw new IllegalArgumentException("Cannot parse name from " + definition);
            }
            name = nameMatcher.group(1);


            String[] profiles = null;
            final Matcher profileMatcher = profilePattern.matcher(definition);
            if (profileMatcher.find()) {
                profiles = StringUtil.splitAndTrim(profileMatcher.group(1), ",").toArray(new String[0]);
            }

            Definition returnObj = new Definition(name, profiles);

            final Matcher propertiesMatcher = propertiesPattern.matcher(definition);
            if (propertiesMatcher.find()) {
                String propertiesString = propertiesMatcher.group(1);
                for (String keyValue : propertiesString.split("&")) {
                    final String[] split = keyValue.split("=");
                    returnObj.properties.put(split[0], split[1]);
                }
            }

            return returnObj;
        }

        private Definition(String name, String... profiles) {
            this.name = name;
            this.profiles = profiles;
        }

        public String getName() {
            return name;
        }

        /**
         * @return the profiles associated with this testSystem. Returns an empty array if no profiles are defined
         */
        public String[] getProfiles() {
            if (profiles == null) {
                return new String[0];
            }
            return Arrays.copyOf(profiles, profiles.length);
        }

        /**
         * @return read-only copy of the local properties in this test system
         */
        public SortedMap<String, Object> getProperties() {
            return Collections.unmodifiableSortedMap(properties);
        }

        @Override
        public String toString() {
            String returnString = getName();
            if (profiles != null && profiles.length > 0) {
                returnString += ":" + StringUtil.join(profiles, ",");
            }

            if (properties.size() > 0) {
                returnString += "?" + StringUtil.join(properties, "&");
            }
            return returnString;
        }

        @Override
        public int hashCode() {
            return toString().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof TestSystem.Definition && this.toString().equals(obj.toString());
        }

        @Override
        public int compareTo(TestSystem.Definition o) {
            if (o == null) {
                return 1;
            }
            return this.toString().compareTo(o.toString());
        }
    }


}
