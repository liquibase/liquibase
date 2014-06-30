package liquibase.statement

import liquibase.AbstractExtensibleObject
import liquibase.AbstractExtensibleObjectTest

public class AutoIncrementConstraintTest extends AbstractExtensibleObjectTest {

    def "constructor"() {
        when:
        AutoIncrementConstraint constraint = new AutoIncrementConstraint("COL_NAME");

        then:
        constraint.getColumnName() == "COL_NAME"
    }
}
