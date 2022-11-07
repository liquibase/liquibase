package liquibase.configuration

import liquibase.configuration.core.SystemPropertyValueProvider
import spock.lang.Specification

class ProvidedValueTest extends Specification {

    def describe() {
        expect:
        new ProvidedValue("requested.key", "actual.key", "value", "test source", new SystemPropertyValueProvider()).describe() == "test source 'actual.key'"
    }
}
