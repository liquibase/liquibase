package liquibase.extension.testing.testsystem.configuration

import liquibase.extension.testing.LiquibaseSdkConfigurationValueProvider
import spock.lang.Specification

class TestEnvironmentConfigurationValueProviderTest extends Specification {

    def "getProvidedValue"() {
        when:
        def provider = new LiquibaseSdkConfigurationValueProvider()

        then:
        provider.getProvidedValue("liquibase.sdk.env.postgresql.username").getValue() == "lbuser"
        provider.getProvidedValue("liquibase.sdk.env.mssql.username") == null //overriden at database level
    }
}
