package liquibase.diff.output.changelog

import liquibase.JUnitScope
import spock.lang.Specification

class ActionGeneratorFactoryTest extends Specification {

    def "finds ActionGenerators"() {
        JUnitScope.instance.getSingleton(ActionGeneratorFactory).registry.size() > 0
    }
}
