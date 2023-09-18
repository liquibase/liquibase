package liquibase.util

import liquibase.UpdateSummaryEnum
import spock.lang.Specification

class ValueHandlerUtilTest extends Specification {
    def "Should populate correct enum value"() {
        expect:
        //This shows as a warning but passes as expected. Not sure what to do about that.
        ValueHandlerUtil.getEnum(clazz, input, "ignored in this test") == expected

        where:
        clazz                   | input     | expected
        UpdateSummaryEnum.class | "OFF"     | UpdateSummaryEnum.OFF
        UpdateSummaryEnum.class | "SUMMARY" | UpdateSummaryEnum.SUMMARY
        UpdateSummaryEnum.class | "VERBOSE" | UpdateSummaryEnum.VERBOSE
    }

    def "Should log warning using parameterName when unable to find valid value"() {
        when:
        //This shows as a warning but passes as expected. Not sure what to do about that.
        ValueHandlerUtil.getEnum(clazz, input, parameterName)
        then:
        def e = thrown(IllegalArgumentException)
        e.message == expected

        where:
        clazz                    | input  | parameterName  | expected
        UpdateSummaryEnum.class  | "blah" | "Summary Mode" | "WARNING: The summary mode value '$input' is not valid. Valid values include: 'OFF', 'SUMMARY', 'VERBOSE'"
    }

    def "Should parse boolean values"() {
        expect:
        ValueHandlerUtil.booleanValueHandler(value) == expected

        where:
        value   | expected
        "true"  | Boolean.TRUE
        "TRUE"  | Boolean.TRUE
        "false" | Boolean.FALSE
        "FALSE" | Boolean.FALSE
        "fAlSe" | Boolean.FALSE
    }
}
