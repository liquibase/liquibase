package liquibase.util

import liquibase.Scope
import liquibase.configuration.ConfigurationValueProvider
import liquibase.configuration.LiquibaseConfiguration
import liquibase.configuration.core.DefaultsFileValueProvider
import liquibase.extension.testing.setup.TestSetup
import liquibase.extension.testing.setup.TestSetupEnvironment

class SetupDefaultsFile extends TestSetup {
    private String defaultsFile
    private Properties properties

    SetupDefaultsFile(String defaultsFile, Properties properties) {
        this.defaultsFile = defaultsFile
        this.properties = properties
    }

    @Override
    void setup(TestSetupEnvironment testSetupEnvironment) throws Exception {
        //
        // We are creating a new properties file
        // We have to remove any previous DefaultsFileValueProvider to make
        // sure the command under test will use the one we just created
        //
        OutputStream outputStream = null
        try {
            outputStream = new FileOutputStream(defaultsFile)
            properties.store(outputStream, "defaults file")
            final File df = new File(defaultsFile)
            final DefaultsFileValueProvider fileProvider = new DefaultsFileValueProvider(df)
            final LiquibaseConfiguration liquibaseConfiguration = Scope.getCurrentScope().getSingleton(LiquibaseConfiguration.class);
            removeDefaultsFileProviders()
            liquibaseConfiguration.registerProvider(fileProvider)
        }
        catch (Throwable e) {
            throw new RuntimeException(e)
        }
        finally {
            if (outputStream != null) {
                outputStream.close()
            }
        }
    }

    @Override
    public void cleanup() {
        def file = new File(defaultsFile)
        if (file.exists()) {
           file.delete()
        }
        removeDefaultsFileProviders()
    }

    //
    // Unregister any existing DefaultsFileValueProvider instances
    //
    private static void removeDefaultsFileProviders() {
        List<ConfigurationValueProvider> toRemove = new ArrayList<>()
        final LiquibaseConfiguration liquibaseConfiguration = Scope.getCurrentScope().getSingleton(LiquibaseConfiguration.class);
        SortedSet<ConfigurationValueProvider> providers = liquibaseConfiguration.getProviders()
        for (ConfigurationValueProvider provider : providers) {
            if (provider instanceof DefaultsFileValueProvider) {
                toRemove.add(provider)
            }
        }
        for (ConfigurationValueProvider candidate : toRemove) {
            liquibaseConfiguration.unregisterProvider(candidate)
        }
    }
}
