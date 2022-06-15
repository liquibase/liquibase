package liquibase.extension.testing.setup;

import groovy.lang.Closure;
import liquibase.Scope;
import liquibase.configuration.ConfigurationValueProvider;
import liquibase.configuration.LiquibaseConfiguration;

/**
 * Setup class allowing registration of custom configuration value providers, and also providing automatic cleanup.
 */
public class SetupConfigurationValueProvider extends TestSetup {

    private final Closure<ConfigurationValueProvider> configurationValueProvider;
    /**
     * Once the configuration value provider has been instantiated, save it here.
     */
    private ConfigurationValueProvider actualConfigurationValueProvider = null;

    /**
     * Create a new configuration value provider.
     * @param configurationValueProvider This is a closure so that instantiation can be "lazy", such that you can
     *                                   create configuration value providers which depend on other TestSetup resources,
     *                                   like newly created files, for example.
     */
    public SetupConfigurationValueProvider(Closure<ConfigurationValueProvider> configurationValueProvider) {
        this.configurationValueProvider = configurationValueProvider;
    }

    @Override
    public void setup(TestSetupEnvironment testSetupEnvironment) throws Exception {
        final LiquibaseConfiguration liquibaseConfiguration = Scope.getCurrentScope().getSingleton(LiquibaseConfiguration.class);
        actualConfigurationValueProvider = configurationValueProvider.call();
        liquibaseConfiguration.registerProvider(actualConfigurationValueProvider);
    }

    @Override
    public void cleanup() {
        LiquibaseConfiguration lbConf = Scope.getCurrentScope().getSingleton(LiquibaseConfiguration.class);
        lbConf.unregisterProvider(actualConfigurationValueProvider);
    }
}
