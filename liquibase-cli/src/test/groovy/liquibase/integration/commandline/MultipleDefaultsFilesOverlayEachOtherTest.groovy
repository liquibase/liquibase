package liquibase.integration.commandline;

import java.net.URL
import java.nio.file.Path
import java.nio.file.Paths
import java.util.regex.Pattern

import liquibase.Scope
import liquibase.configuration.ConfigurationValueConverter
import liquibase.configuration.ConfigurationValueProvider
import liquibase.configuration.LiquibaseConfiguration
import liquibase.util.ObjectUtil
import spock.lang.Specification
import spock.lang.Unroll

public class MultipleDefaultsFilesOverlayEachOtherTest extends Specification {

    @Unroll
    def overlay() {
        given:
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader()
        URL properties = contextClassLoader.getResource("test.properties")
        Path path = Paths.get(properties.toURI()).getParent();
        URL url1 = path.toUri().toURL()
        URL url2 = path.resolve("subfolder").toUri().toURL()
        def urls = [ url1, url2 ]
        URLClassLoader classLoader = new URLClassLoader(urls.toArray(new URL[0]), contextClassLoader)
        Thread.currentThread().setContextClassLoader(classLoader)
        LiquibaseCommandLine liquibaseCommandLine = new LiquibaseCommandLine()
        def args = ["--defaults-file=test.properties"]
        List<ConfigurationValueProvider> valueProviders = liquibaseCommandLine.registerValueProviders(args.toArray(new String[0]))
        LiquibaseConfiguration liquibaseConfiguration = Scope.getCurrentScope().getSingleton(LiquibaseConfiguration.class)

        when:
        ConfigurationValueConverter<String> valueConverter = { value -> ObjectUtil.convert(value, String.class) }

        then:
        liquibaseConfiguration.getCurrentConfiguredValue(valueConverter, null, input).getValue() == expected

        cleanup:
        if (valueProviders != null) {
            for (ConfigurationValueProvider provider : valueProviders) {
                liquibaseConfiguration.unregisterProvider(provider)
            }
        }
        Thread.currentThread().setContextClassLoader(contextClassLoader)
        if (classLoader != null) {
            classLoader.close()
        }

        where:
        input | expected
        "AAA" | "AAA"
        "BBB" | "bbb"
        "CCC" | "ccc"
        "DDD" | null
    }
}
