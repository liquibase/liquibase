package liquibase.extension.testing.testsystem;

import liquibase.Scope;
import liquibase.configuration.ConfigurationValueConverter;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.plugin.AbstractPluginFactory;
import liquibase.util.CollectionUtil;
import liquibase.util.StringUtil;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Factory for getting {@link TestSystem} implementations.
 */
public class TestSystemFactory extends AbstractPluginFactory<TestSystem> {

    private Map<TestSystem.Definition, TestSystem> systems = new HashMap<>();

    @Override
    protected Class<TestSystem> getPluginClass() {
        return TestSystem.class;
    }

    @Override
    protected int getPriority(TestSystem testSystem, Object... args) {
        return testSystem.getPriority((TestSystem.Definition) args[0]);
    }

    /**
     * Return the {@link TestSystem} for the given {@link liquibase.extension.testing.testsystem.TestSystem.Definition}.
     * Returns singleton instances for equal definitions.
     */
    public TestSystem getTestSystem(TestSystem.Definition definition) {
        return systems.computeIfAbsent(definition, passedDefinition -> {
            final TestSystem singleton = TestSystemFactory.this.getPlugin(passedDefinition);

            if (singleton == null) {
                throw new UnexpectedLiquibaseException("No test system: " + passedDefinition);
            }

            try {
                TestSystem.Definition finalDefinition = passedDefinition;
                if (passedDefinition.getProfiles().length == 0 && passedDefinition.getProperties().size() == 0) {
                    //see if we're configured to use a specific setting instead
                    final String testSystems = Scope.getCurrentScope().getSingleton(LiquibaseConfiguration.class).getCurrentConfiguredValue(ConfigurationValueConverter.STRING, null, "liquibase.sdk.testSystem.test").getValue();
                    for (String testSystem : CollectionUtil.createIfNull(StringUtil.splitAndTrim(testSystems, ","))) {
                        final TestSystem.Definition testSystemDef = TestSystem.Definition.parse(testSystem);
                        if (testSystemDef.getName().equals(passedDefinition.getName())) {
                            finalDefinition = testSystemDef;
                            break;
                        }
                    }
                }

                final Constructor<? extends TestSystem> pluginConstructor = singleton.getClass().getConstructor(TestSystem.Definition.class);
                return pluginConstructor.newInstance(finalDefinition);
            } catch (ReflectiveOperationException e) {
                throw new UnexpectedLiquibaseException(e);
            }
        });
    }

    /**
     * Conveniene method for {@link #getTestSystem(TestSystem.Definition)} without having to parse the definition yourself.
     */
    public TestSystem getTestSystem(String definition) {
        return getTestSystem(TestSystem.Definition.parse(definition));
    }

    public Set<String> getTestSystemNames() {
        return super.findAllInstances().stream()
                .map(testSystem -> testSystem.getDefinition().getName())
                .collect(Collectors.toSet());
    }

    @Override
    protected synchronized Collection<TestSystem> findAllInstances() {
        return super.findAllInstances();
    }
}
