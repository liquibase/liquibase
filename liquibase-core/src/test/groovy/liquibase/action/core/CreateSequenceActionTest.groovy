package liquibase.action.core

import spock.lang.Specification
import spock.lang.Unroll

class CreateSequenceActionTest extends Specification {

    @Unroll
    def "describe"() {
        expect:
        object.describe() == expected

        where:
        object                                                                                                                                          | expected
        new CreateSequenceAction()                                                                                                                      | "create sequence()"
        new CreateSequenceAction().setAttribute(CreateSequenceAction.Attr.sequenceName, "test_seq")                                                     | "create sequence(sequenceName=test_seq)"
        new CreateSequenceAction().setAttribute(CreateSequenceAction.Attr.sequenceName, "test_seq").setAttribute(CreateSequenceAction.Attr.cycle, true) | "create sequence(cycle=true,sequenceName=test_seq)"
    }
}
