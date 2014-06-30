package liquibase.statement

import liquibase.AbstractExtensibleObjectTest

class DatabaseFunctionTest extends AbstractExtensibleObjectTest {

    def "toString logic"() {
        expect:
        new DatabaseFunction("value from ctor").toString() == "value from ctor"
    }
}
