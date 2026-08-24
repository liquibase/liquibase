package liquibase.integration.commandline;

import java.net.URL
import java.nio.file.Path
import java.nio.file.Paths

import liquibase.Scope
import liquibase.configuration.ConfigurationValueConverter
import liquibase.configuration.ConfigurationValueProvider
import liquibase.configuration.LiquibaseConfiguration
import liquibase.configuration.core.DefaultsFileValueProvider
import liquibase.resource.ClassLoaderResourceAccessor
import liquibase.resource.Resource
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

        // Mirrors LiquibaseCommandLine#resolveValueProviders: one provider per classpath match, with the
        // precedence offset growing down the list so the first match wins.
        List<ConfigurationValueProvider> valueProviders = new ArrayList<>()
        new ClassLoaderResourceAccessor(classLoader).getAll("test.properties").eachWithIndex { Resource res, int i ->
            res.openInputStream().withCloseable { stream ->
                valueProviders.add(new DefaultsFileValueProvider(stream, "File in classpath " + res.getUri(), i))
            }
        }

        when:
        ConfigurationValueConverter<String> valueConverter = { value -> ObjectUtil.convert(value, String.class) }
        def actual = Scope.child([(LiquibaseConfiguration.SCOPED_VALUE_PROVIDERS_KEY): valueProviders], {
            Scope.getCurrentScope().getSingleton(LiquibaseConfiguration.class)
                    .getCurrentConfiguredValue(valueConverter, null, input).getValue()
        } as Scope.ScopedRunnerWithReturn)

        then:
        actual == expected

        cleanup:
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
