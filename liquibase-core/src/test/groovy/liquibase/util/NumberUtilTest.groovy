package liquibase.util

import spock.lang.Specification
import spock.lang.Unroll

class NumberUtilTest extends Specification {

    @Unroll
    def "parseNumber"() {
        when:
        def output = NumberUtil.parseNumber(input, targetType)

        then:
        output.class == targetType
        output.toString() == input || output.toString() == input + ".0"

        where:
        input  | targetType
        "3"    | Integer
        "3"    | Float
        "3"    | BigDecimal
        "3"    | Byte
        "3"    | Short
        "3"    | Long
        "3"    | BigInteger
        "3"    | Double

        "-3"   | BigInteger
        "-3"   | BigDecimal

        "3.0"  | Float
        "3.0"  | BigDecimal

        "3.5"  | BigDecimal
        "3.5"  | Double
        "-3.5" | BigDecimal
        "-3.5" | Double
    }

}
