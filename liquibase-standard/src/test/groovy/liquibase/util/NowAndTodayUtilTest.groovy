/*
 * Copyright 2018 Sirsi Corporation.  All rights reserved.
 */

package liquibase.util

import liquibase.exception.DateParseException
import spock.lang.Specification
import spock.lang.Unroll

import static java.lang.Math.abs
import static org.junit.Assert.fail

class NowAndTodayUtilTest extends Specification {
    private static final long TIME_DIFF_ALLOWED_IN_MILLIS = 1000 * 5 // 5 seconds in milliseconds.

    static Date today
    static Date lastYear
    static Date nextYear
    static Date yesterday
    static Date tomorrow
    static Date minusTenHours
    static Date plusTenHours
    static Date minusTenMinutes
    static Date plusTenMinutes

    static {
        Calendar c = Calendar.getInstance()
        c.add(Calendar.YEAR, -1)
        lastYear = c.getTime()

        c = Calendar.getInstance()
        c.add(Calendar.YEAR, 1)
        nextYear = c.getTime()

        c = Calendar.getInstance()
        today = c.getTime()
        c.add(Calendar.DATE, -1)

        yesterday = c.getTime()
        c = Calendar.getInstance()
        c.add(Calendar.DATE, 1)

        tomorrow = c.getTime()

        c = Calendar.getInstance()
        c.add(Calendar.HOUR, -10)
        minusTenHours = c.getTime()

        c = Calendar.getInstance()
        c.add(Calendar.HOUR, 10)
        plusTenHours = c.getTime()

        c = Calendar.getInstance()
        c.add(Calendar.MINUTE, -10)
        minusTenMinutes = c.getTime()

        c = Calendar.getInstance()
        c.add(Calendar.MINUTE, 10)
        plusTenMinutes = c.getTime()
    }

    @Unroll
    def "is today format"() {
        expect:
        NowAndTodayUtil.isNowOrTodayFormat(input) == expected

        where:
        input        | expected
        "now"        | true
        "NOW"        | true
        "NOW+1m"     | true
        "NOW-1m"     | true
        "NOW+1h"     | true
        "NOW-1h"     | true
        "NOW+1d"     | true
        "NOW-1d"     | true
        "today"      | true
        "Today"      | true
        "TODAY"      | true
        "today+1"    | true
        "Today-2"    | true
        "TODAY+5"    | true

        null         | false
        ""           | false
        "No"         | false
        "Toda"       | false
        "2015-01-01" | false
    }

    def "doNowOrToday happyPath"() {
        expect:
        isSameDate(NowAndTodayUtil.doNowOrToday(input, type), expected)
        if (type == "DATE") {
            assert NowAndTodayUtil.doNowOrToday(input, type) instanceof java.sql.Date
        } else if (type == "TIME") {
            assert NowAndTodayUtil.doNowOrToday(input, type) instanceof java.sql.Time
        } else if (type == "TIMESTAMP") {
            assert NowAndTodayUtil.doNowOrToday(input, type) instanceof java.sql.Timestamp
        } else if (type == "DATETIME") {
            assert NowAndTodayUtil.doNowOrToday(input, type) instanceof java.sql.Timestamp
        } else {
            fail "unexpected type: $type"
        }

        where:
        input           | type        | expected

        "NOW"           | "DATE"      | today
        "NOW-1y"        | "DATE"      | lastYear
        "NOW-1ye"       | "DATE"      | lastYear
        "NOW-1yea"      | "DATE"      | lastYear
        "NOW-1year"     | "DATE"      | lastYear
        "NOW-1years"    | "DATE"      | lastYear
        "NOW+1y"        | "DATE"      | nextYear

        "NOW-1d"        | "DATE"      | yesterday
        "NOW-1da"       | "DATE"      | yesterday
        "NOW-1day"      | "DATE"      | yesterday
        "NOW-1days"     | "DATE"      | yesterday
        "now+1d"        | "DATE"      | tomorrow

        "now-10h"       | "DATE"      | minusTenHours
        "now-10ho"      | "DATE"      | minusTenHours
        "now-10hou"     | "DATE"      | minusTenHours
        "now-10hour"    | "DATE"      | minusTenHours
        "now-10hours"   | "DATE"      | minusTenHours
        "now+10h"       | "DATE"      | plusTenHours

        "now-10m"       | "DATE"      | minusTenMinutes
        "now-10mi"      | "DATE"      | minusTenMinutes
        "now-10min"     | "DATE"      | minusTenMinutes
        "now-10minu"    | "DATE"      | minusTenMinutes
        "now-10minut"   | "DATE"      | minusTenMinutes
        "now-10minute"  | "DATE"      | minusTenMinutes
        "now-10minutes" | "DATE"      | minusTenMinutes
        "now+10m"       | "DATE"      | plusTenMinutes

        "TODAY"         | "DATE"      | today
        "TODAY-1"       | "DATE"      | yesterday
        "TODAY+1"       | "DATE"      | tomorrow

        "NOW"           | "DATETIME"  | today
        "NOW-1y"        | "DATETIME"  | lastYear
        "NOW-1ye"       | "DATETIME"  | lastYear
        "NOW-1yea"      | "DATETIME"  | lastYear
        "NOW-1year"     | "DATETIME"  | lastYear
        "NOW-1years"    | "DATETIME"  | lastYear
        "NOW+1y"        | "DATETIME"  | nextYear

        "NOW-1d"        | "DATETIME"  | yesterday
        "NOW-1da"       | "DATETIME"  | yesterday
        "NOW-1day"      | "DATETIME"  | yesterday
        "NOW-1days"     | "DATETIME"  | yesterday
        "now+1d"        | "DATETIME"  | tomorrow

        "now-10h"       | "DATETIME"  | minusTenHours
        "now-10ho"      | "DATETIME"  | minusTenHours
        "now-10hou"     | "DATETIME"  | minusTenHours
        "now-10hour"    | "DATETIME"  | minusTenHours
        "now-10hours"   | "DATETIME"  | minusTenHours
        "now+10h"       | "DATETIME"  | plusTenHours

        "now-10m"       | "DATETIME"  | minusTenMinutes
        "now-10mi"      | "DATETIME"  | minusTenMinutes
        "now-10min"     | "DATETIME"  | minusTenMinutes
        "now-10minu"    | "DATETIME"  | minusTenMinutes
        "now-10minut"   | "DATETIME"  | minusTenMinutes
        "now-10minute"  | "DATETIME"  | minusTenMinutes
        "now-10minutes" | "DATETIME"  | minusTenMinutes
        "now+10m"       | "DATETIME"  | plusTenMinutes

        "TODAY"         | "DATETIME"  | today
        "TODAY-1"       | "DATETIME"  | yesterday
        "TODAY+1"       | "DATETIME"  | tomorrow
        "NOW"           | "TIME"      | today
        "TODAY"         | "TIME"      | today
        "NOW-1y"        | "TIME"      | lastYear
        "NOW-1ye"       | "TIME"      | lastYear
        "NOW-1yea"      | "TIME"      | lastYear
        "NOW-1year"     | "TIME"      | lastYear
        "NOW-1years"    | "TIME"      | lastYear
        "NOW+1y"        | "TIME"      | nextYear

        "NOW-1d"        | "TIME"      | yesterday
        "NOW-1da"       | "TIME"      | yesterday
        "NOW-1day"      | "TIME"      | yesterday
        "NOW-1days"     | "TIME"      | yesterday
        "now+1d"        | "TIME"      | tomorrow

        "now-10h"       | "TIME"      | minusTenHours
        "now-10ho"      | "TIME"      | minusTenHours
        "now-10hou"     | "TIME"      | minusTenHours
        "now-10hour"    | "TIME"      | minusTenHours
        "now-10hours"   | "TIME"      | minusTenHours
        "now+10h"       | "TIME"      | plusTenHours

        "now-10m"       | "TIME"      | minusTenMinutes
        "now-10mi"      | "TIME"      | minusTenMinutes
        "now-10min"     | "TIME"      | minusTenMinutes
        "now-10minu"    | "TIME"      | minusTenMinutes
        "now-10minut"   | "TIME"      | minusTenMinutes
        "now-10minute"  | "TIME"      | minusTenMinutes
        "now-10minutes" | "TIME"      | minusTenMinutes
        "now+10m"       | "TIME"      | plusTenMinutes

        "NOW"           | "TIMESTAMP" | today
        "NOW-1y"        | "TIMESTAMP" | lastYear
        "NOW-1ye"       | "TIMESTAMP" | lastYear
        "NOW-1yea"      | "TIMESTAMP" | lastYear
        "NOW-1year"     | "TIMESTAMP" | lastYear
        "NOW-1years"    | "TIMESTAMP" | lastYear
        "NOW+1y"        | "TIMESTAMP" | nextYear

        "NOW-1d"        | "TIMESTAMP" | yesterday
        "NOW-1da"       | "TIMESTAMP" | yesterday
        "NOW-1day"      | "TIMESTAMP" | yesterday
        "NOW-1days"     | "TIMESTAMP" | yesterday
        "now+1d"        | "TIMESTAMP" | tomorrow

        "now-10h"       | "TIMESTAMP" | minusTenHours
        "now-10ho"      | "TIMESTAMP" | minusTenHours
        "now-10hou"     | "TIMESTAMP" | minusTenHours
        "now-10hour"    | "TIMESTAMP" | minusTenHours
        "now-10hours"   | "TIMESTAMP" | minusTenHours
        "now+10h"       | "TIMESTAMP" | plusTenHours

        "now-10m"       | "TIMESTAMP" | minusTenMinutes
        "now-10mi"      | "TIMESTAMP" | minusTenMinutes
        "now-10min"     | "TIMESTAMP" | minusTenMinutes
        "now-10minu"    | "TIMESTAMP" | minusTenMinutes
        "now-10minut"   | "TIMESTAMP" | minusTenMinutes
        "now-10minute"  | "TIMESTAMP" | minusTenMinutes
        "now-10minutes" | "TIMESTAMP" | minusTenMinutes
        "now+10m"       | "TIMESTAMP" | plusTenMinutes
    }

    @Unroll
    def "doNowOrToday invalid values"() {
        expect:
        NowAndTodayUtil.doNowOrToday(input, null) == null

        where:
        input << [
                null,
                "2015 01 01",
                "NO",
                "TODA"
        ]
    }

    @Unroll
    void "doNowOrToday invalidColType"() throws Exception {
        when:
        NowAndTodayUtil.doNowOrToday(input, type)

        then:
        def e = thrown(DateParseException)
        e.message == expectedError

        where:
        input   | type      | expectedError
        "NOW"   | null      | "Must supply non-null column type when using 'NOW' or 'TODAY' value."
        "TODAY" | null      | "Must supply non-null column type when using 'NOW' or 'TODAY' value."

        "NOW"   | "BOOLEAN" | "Unrecognized colType BOOLEAN when using 'NOW' or 'TODAY' value; expected one of date, time, datetime, or timestamp"
        "TODAY" | "BOOLEAN" | "Unrecognized colType BOOLEAN when using 'NOW' or 'TODAY' value; expected one of date, time, datetime, or timestamp"
        "NOW"   | "JUNK"    | "Unrecognized colType JUNK when using 'NOW' or 'TODAY' value; expected one of date, time, datetime, or timestamp"
        "TODAY" | "JUNK"    | "Unrecognized colType JUNK when using 'NOW' or 'TODAY' value; expected one of date, time, datetime, or timestamp"
    }

    /**
     * Asserts that the year, month, and day of month portions of two dates are the same.
     * @param date1 First date
     * @param date2 Second date (lucky guy)
     * @return true if the year, month, and day are the same, false otherwise.
     */
    private static boolean isSameDate(Date date1, Date date2) {
        Calendar testC = Calendar.getInstance()
        testC.setTime(date1)

        Calendar correctC = Calendar.getInstance()
        correctC.setTime(date2)

        if (testC.get(Calendar.YEAR) != correctC.get(Calendar.YEAR)) {
            return false
        }
        if (testC.get(Calendar.MONTH) != correctC.get(Calendar.MONTH)) {
            return false
        }
        if (testC.get(Calendar.DAY_OF_MONTH) != correctC.get(Calendar.DAY_OF_MONTH)) {
            return false
        }
        return true
    }

    /**
     * Asserts that the two dates are within 5 seconds of each other (to allow for flexibility in running the unit test)
     */
    private static boolean isSameTime(Date date1, Date date2) {
        if (date1 == null) {
            return false
        }
        long diffInMillis = abs(date1.getTime() - date2.getTime())
        return diffInMillis < TIME_DIFF_ALLOWED_IN_MILLIS
    }
}
