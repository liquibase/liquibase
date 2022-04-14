package liquibase.logging.core

import com.example.liquibase.change.CreateTableExampleChange
import liquibase.Scope
import liquibase.change.Change
import liquibase.change.core.CreateTableChange
import liquibase.util.StringUtil
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class JavaLogServiceTest extends Specification {

    @Unroll("#featureName: #clazz")
    def "getLogName"() {
        expect:
        new JavaLogService().getLogName(clazz) == expected

        where:
        clazz                             | expected
        Scope.class                       | "liquibase"
        Change.class                      | "liquibase.change"
        CreateTableChange.class           | "liquibase.change"
        StringUtil.class                  | "liquibase.util"
        String.class                      | "liquibase"
        CreateTableExampleChange          | "liquibase.change"
        null                              | "unknown"
    }

    def "getLogName with a null package"() {
        when:
        def noPackageClass = { -> }.getClass()

        def nameField = Class.class.getDeclaredField("name")
        nameField.setAccessible(true)
        nameField.set(noPackageClass, "NoPackageClass")

        then:
        noPackageClass.getPackage() == null
        new JavaLogService().getLogName(noPackageClass) == "NoPackageClass"

    }
}
