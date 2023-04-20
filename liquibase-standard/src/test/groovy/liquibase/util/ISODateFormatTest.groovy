package liquibase.util


import spock.lang.Specification
import spock.lang.Unroll

import java.sql.Time
import java.sql.Timestamp

class ISODateFormatTest extends Specification {

    private ISODateFormat dateFormat = new ISODateFormat()

    @Unroll
    def "parse and reformat"() {
        expect:
        dateFormat.format(dateFormat.parse(input)) == expected

        where:
        input                              | expected
        null                               | null
        "2012-09-12T09:47:54.664"          | "2012-09-12T09:47:54.664" //no leading zero fractions
        "2011-04-21T10:13:40.044"          | "2011-04-21T10:13:40.044" //leading zero fractions
        "2011-04-21T10:13:40"              | "2011-04-21T10:13:40" //no leading fractions
        "2011-04-21T10:13:40.12"           | "2011-04-21T10:13:40.12" //leading fractions
        "2011-04-21T10:13:40.084004Z"      | "2011-04-21T10:13:40.084004" //utc timezone
        "2011-04-21T10:13:40.084004-05:00" | "2011-04-21T10:13:40.084004" //est timezone
        "2011-04-21T10:13:40.01234567"     | "2011-04-21T10:13:40.01234567" //leading nano fractions
        "10:13:40"                         | "10:13:40"
    }

    @Unroll
    def format() {
        expect:
        dateFormat.format(input) == expected
        dateFormat.format((Date) input) == expected

        where:
        input                                | expected
        null                                 | null
        new java.sql.Date(110, 5, 8)         | "2010-06-08"
        new Time(13, 02, 05)                 | "13:02:05"
        new Timestamp(111, 6, 8, 3, 5, 7, 9) | "2011-07-08T03:05:07.000000009"
        new Date(111, 3, 5)                  | "2011-04-05T00:00:00"
        new Date(111, 3, 5, 1, 7, 9)         | "2011-04-05T01:07:09"
    }
}
