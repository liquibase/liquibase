package liquibase.change

import liquibase.Scope
import liquibase.change.core.DropTableChange
import liquibase.util.StringUtil
import spock.lang.Specification

class ChangeFactorySpec extends Specification {

    def "getParameters with parameters set"() {
        when:
        def change = new DropTableChange()
        change.tableName = "tab"
        change.schemaName = "schem"

        then:
        Scope.currentScope.getSingleton(ChangeFactory).getParameters(change) == [tableName: "tab", schemaName: "schem"]
    }

    def "getParameters with no parameters set"() {
        when:
        def change = new DropTableChange()

        then:
        StringUtil.join(Scope.currentScope.getSingleton(ChangeFactory).getParameters(change), ",") == ""
    }
}
