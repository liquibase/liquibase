package liquibase.structure.core

import spock.lang.Specification

class DatabaseObjectFactoryTest extends Specification {

    def "getAllTypes"() {
        expect:
        DatabaseObjectFactory.instance.allTypes.contains(Table)
        DatabaseObjectFactory.instance.allTypes.contains(Column)
        DatabaseObjectFactory.instance.allTypes.contains(View)
        DatabaseObjectFactory.instance.allTypes.contains(Data)
    }

    def "getStandardTypes"() {
        expect:
        DatabaseObjectFactory.instance.standardTypes.contains(Table)
        DatabaseObjectFactory.instance.standardTypes.contains(Column)
        DatabaseObjectFactory.instance.standardTypes.contains(View)

        !DatabaseObjectFactory.instance.standardTypes.contains(Data)
    }
}
