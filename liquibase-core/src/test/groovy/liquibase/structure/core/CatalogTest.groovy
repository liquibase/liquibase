package liquibase.structure.core

import spock.lang.Specification
import spock.lang.Unroll

class CatalogTest extends Specification {

    @Unroll("#featureName: #param")
    def "get/set attributes work"() {
        when:
        def catalog = new Catalog()
        then:
        catalog[param] == defaultValue

        when:
        catalog[param] = newValue
        then:
        catalog[param] == newValue

        where:
        param     | defaultValue | newValue
        "name"    | null         | "catName"
        "default" | true         | true
    }
}
