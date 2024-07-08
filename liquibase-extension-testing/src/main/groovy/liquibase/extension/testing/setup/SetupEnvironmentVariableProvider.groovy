package liquibase.extension.testing.setup

import liquibase.Scope
import liquibase.configuration.ConfigurationValueProvider
import liquibase.configuration.LiquibaseConfiguration
import liquibase.configuration.core.DefaultsFileValueProvider
import liquibase.configuration.core.EnvironmentValueProvider
import liquibase.extension.testing.EnvironmentVariableProviderForTest

class SetupEnvironmentVariableProvider extends TestSetup {
    Map add = [:]
    String[] remove = new String[0]
    List<ConfigurationValueProvider> oldProviders = new ArrayList<>()

    SetupEnvironmentVariableProvider(Map add, String[] remove) {
        this.add = add
        this.remove = remove
    }

    @Override
    void setup(TestSetupEnvironment testSetupEnvironment) throws Exception {
        LiquibaseConfiguration configuration = Scope.getCurrentScope().getSingleton(LiquibaseConfiguration.class)
        Set<ConfigurationValueProvider> providers = configuration.getProviders()
        for (ConfigurationValueProvider provider : providers) {
            if (provider instanceof EnvironmentValueProvider || provider instanceof DefaultsFileValueProvider) {
                oldProviders.add(provider)
                break
            }
        }
        if (oldProviders.size() > 0) {
            for (ConfigurationValueProvider oldProvider : oldProviders) {
                configuration.unregisterProvider(oldProvider)
            }
        }
        configuration.registerProvider(new EnvironmentVariableProviderForTest(add, remove))
    }

    @Override
    void cleanup() {
        if (oldProviders.size() > 0) {
            LiquibaseConfiguration configuration = Scope.getCurrentScope().getSingleton(LiquibaseConfiguration.class)
            Set<ConfigurationValueProvider> providers = configuration.getProviders()
            ConfigurationValueProvider toRemove = null
            for (ConfigurationValueProvider provider : providers) {
                if (provider instanceof EnvironmentVariableProviderForTest) {
                    toRemove = provider
                    break
                }
            }
            if (toRemove) {
                configuration.unregisterProvider(toRemove)
            }
            for (ConfigurationValueProvider oldProvider : oldProviders) {
                configuration.registerProvider(oldProvider)
            }
        }
    }
}
