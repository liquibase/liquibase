/*
 * Copyright 2018 Sirsi Corporation.  All rights reserved.
 */

package liquibase.util;

import liquibase.exception.DateParseException;
import org.junit.Assert;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

import static java.lang.Math.abs;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class NowAndTodayUtilTest {
    private static final long TIME_DIFF_ALLOWED_IN_MILLIS = 1000 * 5; // 5 seconds in milliseconds.


    @Test
    public void test_isTodayFormat()
    {
        assertTrue(NowAndTodayUtil.isNowOrTodayFormat("now"));
        assertTrue(NowAndTodayUtil.isNowOrTodayFormat("NOW"));
        assertTrue(NowAndTodayUtil.isNowOrTodayFormat("NOW+1m"));
        assertTrue(NowAndTodayUtil.isNowOrTodayFormat("NOW-1m"));
        assertTrue(NowAndTodayUtil.isNowOrTodayFormat("NOW+1h"));
        assertTrue(NowAndTodayUtil.isNowOrTodayFormat("NOW-1h"));
        assertTrue(NowAndTodayUtil.isNowOrTodayFormat("NOW+1d"));
        assertTrue(NowAndTodayUtil.isNowOrTodayFormat("NOW-1d"));

        assertTrue(NowAndTodayUtil.isNowOrTodayFormat("today"));
        assertTrue(NowAndTodayUtil.isNowOrTodayFormat("Today"));
        assertTrue(NowAndTodayUtil.isNowOrTodayFormat("TODAY"));
        assertTrue(NowAndTodayUtil.isNowOrTodayFormat("today+1"));
        assertTrue(NowAndTodayUtil.isNowOrTodayFormat("Today-2"));
        assertTrue(NowAndTodayUtil.isNowOrTodayFormat("TODAY+5"));

        assertFalse(NowAndTodayUtil.isNowOrTodayFormat(null));
        assertFalse(NowAndTodayUtil.isNowOrTodayFormat(""));
        assertFalse(NowAndTodayUtil.isNowOrTodayFormat("No"));
        assertFalse(NowAndTodayUtil.isNowOrTodayFormat("Toda"));
        assertFalse(NowAndTodayUtil.isNowOrTodayFormat("2015-01-01"));
    }

    @Test
    public void test_doNowOrToday_NOW_happyPath_DATE()
    {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.YEAR, -1);
        Date lastYear = c.getTime();
        c = Calendar.getInstance();
        c.add(Calendar.YEAR, 1);
        Date nextYear = c.getTime();

        c = Calendar.getInstance();
        Date today = c.getTime();
        c.add(Calendar.DATE, -1);
        Date yesterday = c.getTime();
        c = Calendar.getInstance();
        c.add(Calendar.DATE, 1);
        Date tomorrow = c.getTime();

        c = Calendar.getInstance();
        c.add(Calendar.HOUR, -10);
        Date minusTenHours = c.getTime();
        c = Calendar.getInstance();
        c.add(Calendar.HOUR, 10);
        Date plusTenHours = c.getTime();

        c = Calendar.getInstance();
        c.add(Calendar.MINUTE, -10);
        Date minusTenMinute = c.getTime();
        c = Calendar.getInstance();
        c.add(Calendar.MINUTE, 10);
        Date plusTenMinutes = c.getTime();

        try {
            Assert.assertTrue("NOW failed to match to today's date", isSameDate(NowAndTodayUtil.doNowOrToday("NOW", "DATE"), today));
            Assert.assertTrue("NOW-1y failed to match to last year's date", isSameDate(NowAndTodayUtil.doNowOrToday("NOW-1y", "DATE"), lastYear));
            Assert.assertTrue("NOW-1ye failed to match to last year's date", isSameDate(NowAndTodayUtil.doNowOrToday("NOW-1ye", "DATE"), lastYear));
            Assert.assertTrue("NOW-1yea failed to match to last year's date", isSameDate(NowAndTodayUtil.doNowOrToday("NOW-1yea", "DATE"), lastYear));
            Assert.assertTrue("NOW-1year failed to match to last year's date", isSameDate(NowAndTodayUtil.doNowOrToday("NOW-1year", "DATE"), lastYear));
            Assert.assertTrue("NOW-1years failed to match to last year's date", isSameDate(NowAndTodayUtil.doNowOrToday("NOW-1years", "DATE"), lastYear));
            Assert.assertTrue("NOW+1y failed to match to next year's date", isSameDate(NowAndTodayUtil.doNowOrToday("NOW+1y", "DATE"), nextYear));

            Assert.assertTrue("NOW-1d failed to match to yesterday's date", isSameDate(NowAndTodayUtil.doNowOrToday("NOW-1d", "DATE"), yesterday));
            Assert.assertTrue("NOW-1da failed to match to yesterday's date", isSameDate(NowAndTodayUtil.doNowOrToday("NOW-1da", "DATE"), yesterday));
            Assert.assertTrue("NOW-1day failed to match to yesterday's date", isSameDate(NowAndTodayUtil.doNowOrToday("NOW-1day", "DATE"), yesterday));
            Assert.assertTrue("NOW-1days failed to match to yesterday's date", isSameDate(NowAndTodayUtil.doNowOrToday("NOW-1days", "DATE"), yesterday));
            Assert.assertTrue("NOW+1d failed to match to tomorrow's date", isSameDate(NowAndTodayUtil.doNowOrToday("now+1d", "DATE"), tomorrow));

            Assert.assertTrue("NOW-10h failed to match to 1 minute ago", isSameTime(NowAndTodayUtil.doNowOrToday("now-10h", "DATE"), minusTenHours));
            Assert.assertTrue("NOW-10ho failed to match to 1 minute ago", isSameTime(NowAndTodayUtil.doNowOrToday("now-10ho", "DATE"), minusTenHours));
            Assert.assertTrue("NOW-10hou failed to match to 1 minute ago", isSameTime(NowAndTodayUtil.doNowOrToday("now-10hou", "DATE"), minusTenHours));
            Assert.assertTrue("NOW-10hour failed to match to 1 minute ago", isSameTime(NowAndTodayUtil.doNowOrToday("now-10hour", "DATE"), minusTenHours));
            Assert.assertTrue("NOW-10hours failed to match to 1 minute ago", isSameTime(NowAndTodayUtil.doNowOrToday("now-10hours", "DATE"), minusTenHours));
            Assert.assertTrue("NOW+10h failed to match to 1 minute ago", isSameTime(NowAndTodayUtil.doNowOrToday("now+10h", "DATE"), plusTenHours));

            Assert.assertTrue("NOW-10m failed to match to 1 minute ago", isSameTime(NowAndTodayUtil.doNowOrToday("now-10m", "DATE"), minusTenMinute));
            Assert.assertTrue("NOW-10mi failed to match to 1 minute ago", isSameTime(NowAndTodayUtil.doNowOrToday("now-10mi", "DATE"), minusTenMinute));
            Assert.assertTrue("NOW-10min failed to match to 1 minute ago", isSameTime(NowAndTodayUtil.doNowOrToday("now-10min", "DATE"), minusTenMinute));
            Assert.assertTrue("NOW-10minu failed to match to 1 minute ago", isSameTime(NowAndTodayUtil.doNowOrToday("now-10minu", "DATE"), minusTenMinute));
            Assert.assertTrue("NOW-10minut failed to match to 1 minute ago", isSameTime(NowAndTodayUtil.doNowOrToday("now-10minut", "DATE"), minusTenMinute));
            Assert.assertTrue("NOW-10minute failed to match to 1 minute ago", isSameTime(NowAndTodayUtil.doNowOrToday("now-10minute", "DATE"), minusTenMinute));
            Assert.assertTrue("NOW-10minutes failed to match to 1 minute ago", isSameTime(NowAndTodayUtil.doNowOrToday("now-10minutes", "DATE"), minusTenMinute));
            Assert.assertTrue("NOW+10m failed to match to 1 minute ago", isSameTime(NowAndTodayUtil.doNowOrToday("now+10m", "DATE"), plusTenMinutes));
            Assert.assertNull(NowAndTodayUtil.doNowOrToday(null, null));
            Assert.assertNull(NowAndTodayUtil.doNowOrToday("2015 01 01", null));
            Assert.assertNull(NowAndTodayUtil.doNowOrToday("NO", null));
        } catch (DateParseException e) {
            fail("Unexpected exception running DATE happy path NOW test: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    public void test_doNowOrToday_TODAY_happyPath_DATE()
    {
        Calendar c = Calendar.getInstance();
        Date today = c.getTime();
        c.add(Calendar.DATE, -1);
        Date yesterday = c.getTime();
        c = Calendar.getInstance();
        c.add(Calendar.DATE, 1);
        Date tomorrow = c.getTime();

        try {
            Assert.assertTrue("TODAY failed to match to today's date", isSameDate(NowAndTodayUtil.doNowOrToday("TODAY", "DATE"), today));
            Assert.assertTrue("TODAY-1 failed to match to yesterday's date", isSameDate(NowAndTodayUtil.doNowOrToday("TODAY-1", "DATE"), yesterday));
            Assert.assertTrue("TODAY+1 failed to match to tomorrow's date", isSameDate(NowAndTodayUtil.doNowOrToday("TODAY+1", "DATE"), tomorrow));
            Assert.assertNull(NowAndTodayUtil.doNowOrToday(null, null));
            Assert.assertNull(NowAndTodayUtil.doNowOrToday("2015 01 01", null));
            Assert.assertNull(NowAndTodayUtil.doNowOrToday("TODA", null));
        } catch (DateParseException e) {
            fail("Unexpected exception running DATE happy path TODAY test: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    public void test_doNowOrToday_NOW_happyPath_DATETIME()
    {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.YEAR, -1);
        Date lastYear = c.getTime();
        c = Calendar.getInstance();
        c.add(Calendar.YEAR, 1);
        Date nextYear = c.getTime();

        c = Calendar.getInstance();
        Date today = c.getTime();
        c.add(Calendar.DATE, -1);
        Date yesterday = c.getTime();
        c = Calendar.getInstance();
        c.add(Calendar.DATE, 1);
        Date tomorrow = c.getTime();

        c = Calendar.getInstance();
        c.add(Calendar.HOUR, -10);
        Date minusTenHours = c.getTime();
        c = Calendar.getInstance();
        c.add(Calendar.HOUR, 10);
        Date plusTenHours = c.getTime();

        c = Calendar.getInstance();
        c.add(Calendar.MINUTE, -10);
        Date minusTenMinute = c.getTime();
        c = Calendar.getInstance();
        c.add(Calendar.MINUTE, 10);
        Date plusTenMinutes = c.getTime();

        try {
            Assert.assertTrue("NOW failed to match to today's date", isSameDate(NowAndTodayUtil.doNowOrToday("NOW", "DATETIME"), today));
            Assert.assertTrue("NOW-1y failed to match to last year's date", isSameDate(NowAndTodayUtil.doNowOrToday("NOW-1y", "DATETIME"), lastYear));
            Assert.assertTrue("NOW-1ye failed to match to last year's date", isSameDate(NowAndTodayUtil.doNowOrToday("NOW-1ye", "DATETIME"), lastYear));
            Assert.assertTrue("NOW-1yea failed to match to last year's date", isSameDate(NowAndTodayUtil.doNowOrToday("NOW-1yea", "DATETIME"), lastYear));
            Assert.assertTrue("NOW-1year failed to match to last year's date", isSameDate(NowAndTodayUtil.doNowOrToday("NOW-1year", "DATETIME"), lastYear));
            Assert.assertTrue("NOW-1years failed to match to last year's date", isSameDate(NowAndTodayUtil.doNowOrToday("NOW-1years", "DATETIME"), lastYear));
            Assert.assertTrue("NOW+1y failed to match to next year's date", isSameDate(NowAndTodayUtil.doNowOrToday("NOW+1y", "DATETIME"), nextYear));

            Assert.assertTrue("NOW-1d failed to match to yesterday's date", isSameDate(NowAndTodayUtil.doNowOrToday("NOW-1d", "DATETIME"), yesterday));
            Assert.assertTrue("NOW-1da failed to match to yesterday's date", isSameDate(NowAndTodayUtil.doNowOrToday("NOW-1da", "DATETIME"), yesterday));
            Assert.assertTrue("NOW-1day failed to match to yesterday's date", isSameDate(NowAndTodayUtil.doNowOrToday("NOW-1day", "DATETIME"), yesterday));
            Assert.assertTrue("NOW-1days failed to match to yesterday's date", isSameDate(NowAndTodayUtil.doNowOrToday("NOW-1days", "DATETIME"), yesterday));
            Assert.assertTrue("NOW+1d failed to match to tomorrow's date", isSameDate(NowAndTodayUtil.doNowOrToday("now+1d", "DATETIME"), tomorrow));

            Assert.assertTrue("NOW-10h failed to match to 1 minute ago", isSameTime(NowAndTodayUtil.doNowOrToday("now-10h", "DATETIME"), minusTenHours));
            Assert.assertTrue("NOW-10ho failed to match to 1 minute ago", isSameTime(NowAndTodayUtil.doNowOrToday("now-10ho", "DATETIME"), minusTenHours));
            Assert.assertTrue("NOW-10hou failed to match to 1 minute ago", isSameTime(NowAndTodayUtil.doNowOrToday("now-10hou", "DATETIME"), minusTenHours));
            Assert.assertTrue("NOW-10hour failed to match to 1 minute ago", isSameTime(NowAndTodayUtil.doNowOrToday("now-10hour", "DATETIME"), minusTenHours));
            Assert.assertTrue("NOW-10hours failed to match to 1 minute ago", isSameTime(NowAndTodayUtil.doNowOrToday("now-10hours", "DATETIME"), minusTenHours));
            Assert.assertTrue("NOW+10h failed to match to 1 minute ago", isSameTime(NowAndTodayUtil.doNowOrToday("now+10h", "DATETIME"), plusTenHours));

            Assert.assertTrue("NOW-10m failed to match to 1 minute ago", isSameTime(NowAndTodayUtil.doNowOrToday("now-10m", "DATETIME"), minusTenMinute));
            Assert.assertTrue("NOW-10mi failed to match to 1 minute ago", isSameTime(NowAndTodayUtil.doNowOrToday("now-10mi", "DATETIME"), minusTenMinute));
            Assert.assertTrue("NOW-10min failed to match to 1 minute ago", isSameTime(NowAndTodayUtil.doNowOrToday("now-10min", "DATETIME"), minusTenMinute));
            Assert.assertTrue("NOW-10minu failed to match to 1 minute ago", isSameTime(NowAndTodayUtil.doNowOrToday("now-10minu", "DATETIME"), minusTenMinute));
            Assert.assertTrue("NOW-10minut failed to match to 1 minute ago", isSameTime(NowAndTodayUtil.doNowOrToday("now-10minut", "DATETIME"), minusTenMinute));
            Assert.assertTrue("NOW-10minute failed to match to 1 minute ago", isSameTime(NowAndTodayUtil.doNowOrToday("now-10minute", "DATETIME"), minusTenMinute));
            Assert.assertTrue("NOW-10minutes failed to match to 1 minute ago", isSameTime(NowAndTodayUtil.doNowOrToday("now-10minutes", "DATETIME"), minusTenMinute));
            Assert.assertTrue("NOW+10m failed to match to 1 minute ago", isSameTime(NowAndTodayUtil.doNowOrToday("now+10m", "DATETIME"), plusTenMinutes));
            Assert.assertNull(NowAndTodayUtil.doNowOrToday(null, null));
            Assert.assertNull(NowAndTodayUtil.doNowOrToday("2015 01 01", null));
            Assert.assertNull(NowAndTodayUtil.doNowOrToday("NO", null));
        } catch (DateParseException e) {
            fail("Unexpected exception running DATETIME happy path NOW test: " + e.getMessage());
            e.printStackTrace();
        }
    }


    @Test
    public void test_doNowOrToday_TODAY_happyPath_DATETIME()
    {
        Calendar c = Calendar.getInstance();
        Date today = c.getTime();
        c.add(Calendar.DATE, -1);
        Date yesterday = c.getTime();
        c = Calendar.getInstance();
        c.add(Calendar.DATE, 1);
        Date tomorrow = c.getTime();

        try {
            Assert.assertTrue("TODAY failed to match to today's date", isSameDate(NowAndTodayUtil.doNowOrToday("TODAY", "DATETIME"), today));
            Assert.assertTrue("TODAY-1 failed to match to yesterday's date", isSameDate(NowAndTodayUtil.doNowOrToday("TODAY-1", "DATETIME"), yesterday));
            Assert.assertTrue("TODAY+1 failed to match to tomorrow's date", isSameDate(NowAndTodayUtil.doNowOrToday("TODAY+1", "DATETIME"), tomorrow));
            Assert.assertNull(NowAndTodayUtil.doNowOrToday(null, null));
            Assert.assertNull(NowAndTodayUtil.doNowOrToday("2015 01 01", null));
            Assert.assertNull(NowAndTodayUtil.doNowOrToday("TODA", null));
        } catch (DateParseException e) {
            fail("Unexpected exception running DATETIME happy path TODAY test: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    public void test_doNowOrToday_NOW_happyPath_TIME()
    {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.YEAR, -1);
        Date lastYear = c.getTime();
        c = Calendar.getInstance();
        c.add(Calendar.YEAR, 1);
        Date nextYear = c.getTime();

        c = Calendar.getInstance();
        Date today = c.getTime();
        c.add(Calendar.DATE, -1);
        Date yesterday = c.getTime();
        c = Calendar.getInstance();
        c.add(Calendar.DATE, 1);
        Date tomorrow = c.getTime();

        c = Calendar.getInstance();
        c.add(Calendar.HOUR, -10);
        Date minusTenHours = c.getTime();
        c = Calendar.getInstance();
        c.add(Calendar.HOUR, 10);
        Date plusTenHours = c.getTime();

        c = Calendar.getInstance();
        c.add(Calendar.MINUTE, -10);
        Date minusTenMinute = c.getTime();
        c = Calendar.getInstance();
        c.add(Calendar.MINUTE, 10);
        Date plusTenMinutes = c.getTime();

        try {
            Assert.assertTrue("NOW failed to match to today's date", isSameDate(NowAndTodayUtil.doNowOrToday("NOW", "TIME"), today));
            Assert.assertTrue("NOW-1y failed to match to last year's date", isSameDate(NowAndTodayUtil.doNowOrToday("NOW-1y", "TIME"), lastYear));
            Assert.assertTrue("NOW-1ye failed to match to last year's date", isSameDate(NowAndTodayUtil.doNowOrToday("NOW-1ye", "TIME"), lastYear));
            Assert.assertTrue("NOW-1yea failed to match to last year's date", isSameDate(NowAndTodayUtil.doNowOrToday("NOW-1yea", "TIME"), lastYear));
            Assert.assertTrue("NOW-1year failed to match to last year's date", isSameDate(NowAndTodayUtil.doNowOrToday("NOW-1year", "TIME"), lastYear));
            Assert.assertTrue("NOW-1years failed to match to last year's date", isSameDate(NowAndTodayUtil.doNowOrToday("NOW-1years", "TIME"), lastYear));
            Assert.assertTrue("NOW+1y failed to match to next year's date", isSameDate(NowAndTodayUtil.doNowOrToday("NOW+1y", "TIME"), nextYear));

            Assert.assertTrue("NOW-1d failed to match to yesterday's date", isSameDate(NowAndTodayUtil.doNowOrToday("NOW-1d", "TIME"), yesterday));
            Assert.assertTrue("NOW-1da failed to match to yesterday's date", isSameDate(NowAndTodayUtil.doNowOrToday("NOW-1da", "TIME"), yesterday));
            Assert.assertTrue("NOW-1day failed to match to yesterday's date", isSameDate(NowAndTodayUtil.doNowOrToday("NOW-1day", "TIME"), yesterday));
            Assert.assertTrue("NOW-1days failed to match to yesterday's date", isSameDate(NowAndTodayUtil.doNowOrToday("NOW-1days", "TIME"), yesterday));
            Assert.assertTrue("NOW+1d failed to match to tomorrow's date", isSameDate(NowAndTodayUtil.doNowOrToday("now+1d", "TIME"), tomorrow));

            Assert.assertTrue("NOW-10h failed to match to 1 minute ago", isSameTime(NowAndTodayUtil.doNowOrToday("now-10h", "TIME"), minusTenHours));
            Assert.assertTrue("NOW-10ho failed to match to 1 minute ago", isSameTime(NowAndTodayUtil.doNowOrToday("now-10ho", "TIME"), minusTenHours));
            Assert.assertTrue("NOW-10hou failed to match to 1 minute ago", isSameTime(NowAndTodayUtil.doNowOrToday("now-10hou", "TIME"), minusTenHours));
            Assert.assertTrue("NOW-10hour failed to match to 1 minute ago", isSameTime(NowAndTodayUtil.doNowOrToday("now-10hour", "TIME"), minusTenHours));
            Assert.assertTrue("NOW-10hours failed to match to 1 minute ago", isSameTime(NowAndTodayUtil.doNowOrToday("now-10hours", "TIME"), minusTenHours));
            Assert.assertTrue("NOW+10h failed to match to 1 minute ago", isSameTime(NowAndTodayUtil.doNowOrToday("now+10h", "TIME"), plusTenHours));

            Assert.assertTrue("NOW-10m failed to match to 1 minute ago", isSameTime(NowAndTodayUtil.doNowOrToday("now-10m", "TIME"), minusTenMinute));
            Assert.assertTrue("NOW-10mi failed to match to 1 minute ago", isSameTime(NowAndTodayUtil.doNowOrToday("now-10mi", "TIME"), minusTenMinute));
            Assert.assertTrue("NOW-10min failed to match to 1 minute ago", isSameTime(NowAndTodayUtil.doNowOrToday("now-10min", "TIME"), minusTenMinute));
            Assert.assertTrue("NOW-10minu failed to match to 1 minute ago", isSameTime(NowAndTodayUtil.doNowOrToday("now-10minu", "TIME"), minusTenMinute));
            Assert.assertTrue("NOW-10minut failed to match to 1 minute ago", isSameTime(NowAndTodayUtil.doNowOrToday("now-10minut", "TIME"), minusTenMinute));
            Assert.assertTrue("NOW-10minute failed to match to 1 minute ago", isSameTime(NowAndTodayUtil.doNowOrToday("now-10minute", "TIME"), minusTenMinute));
            Assert.assertTrue("NOW-10minutes failed to match to 1 minute ago", isSameTime(NowAndTodayUtil.doNowOrToday("now-10minutes", "TIME"), minusTenMinute));
            Assert.assertTrue("NOW+10m failed to match to 1 minute ago", isSameTime(NowAndTodayUtil.doNowOrToday("now+10m", "TIME"), plusTenMinutes));
            Assert.assertNull(NowAndTodayUtil.doNowOrToday(null, null));
            Assert.assertNull(NowAndTodayUtil.doNowOrToday("2015 01 01", null));
            Assert.assertNull(NowAndTodayUtil.doNowOrToday("NO", null));
        } catch (DateParseException e) {
            fail("Unexpected exception running TIME happy path NOW test: " + e.getMessage());
            e.printStackTrace();
        }
    }


    @Test
    public void test_doNowOrToday_TODAY_happyPath_TIME()
    {
        try {
            Date res = NowAndTodayUtil.doNowOrToday("TODAY", "TIME");
            assertTrue(res instanceof java.sql.Time);
            String time = res.toString();
            assertTrue(Pattern.matches("\\d\\d:\\d\\d:\\d\\d", time));
        } catch (DateParseException e) {
            fail("Unexpected exception running TIME happy path TODAY test: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    public void test_doNowOrToday_NOW_happyPath_TIMESTAMP()
    {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.YEAR, -1);
        Date lastYear = c.getTime();
        c = Calendar.getInstance();
        c.add(Calendar.YEAR, 1);
        Date nextYear = c.getTime();

        c = Calendar.getInstance();
        Date today = c.getTime();
        c.add(Calendar.DATE, -1);
        Date yesterday = c.getTime();
        c = Calendar.getInstance();
        c.add(Calendar.DATE, 1);
        Date tomorrow = c.getTime();

        c = Calendar.getInstance();
        c.add(Calendar.HOUR, -10);
        Date minusTenHours = c.getTime();
        c = Calendar.getInstance();
        c.add(Calendar.HOUR, 10);
        Date plusTenHours = c.getTime();

        c = Calendar.getInstance();
        c.add(Calendar.MINUTE, -10);
        Date minusTenMinute = c.getTime();
        c = Calendar.getInstance();
        c.add(Calendar.MINUTE, 10);
        Date plusTenMinutes = c.getTime();

        try {
            Assert.assertTrue("NOW failed to match to today's date", isSameDate(NowAndTodayUtil.doNowOrToday("NOW", "TIMESTAMP"), today));
            Assert.assertTrue("NOW-1y failed to match to last year's date", isSameDate(NowAndTodayUtil.doNowOrToday("NOW-1y", "TIMESTAMP"), lastYear));
            Assert.assertTrue("NOW-1ye failed to match to last year's date", isSameDate(NowAndTodayUtil.doNowOrToday("NOW-1ye", "TIMESTAMP"), lastYear));
            Assert.assertTrue("NOW-1yea failed to match to last year's date", isSameDate(NowAndTodayUtil.doNowOrToday("NOW-1yea", "TIMESTAMP"), lastYear));
            Assert.assertTrue("NOW-1year failed to match to last year's date", isSameDate(NowAndTodayUtil.doNowOrToday("NOW-1year", "TIMESTAMP"), lastYear));
            Assert.assertTrue("NOW-1years failed to match to last year's date", isSameDate(NowAndTodayUtil.doNowOrToday("NOW-1years", "TIMESTAMP"), lastYear));
            Assert.assertTrue("NOW+1y failed to match to next year's date", isSameDate(NowAndTodayUtil.doNowOrToday("NOW+1y", "TIMESTAMP"), nextYear));

            Assert.assertTrue("NOW-1d failed to match to yesterday's date", isSameDate(NowAndTodayUtil.doNowOrToday("NOW-1d", "TIMESTAMP"), yesterday));
            Assert.assertTrue("NOW-1da failed to match to yesterday's date", isSameDate(NowAndTodayUtil.doNowOrToday("NOW-1da", "TIMESTAMP"), yesterday));
            Assert.assertTrue("NOW-1day failed to match to yesterday's date", isSameDate(NowAndTodayUtil.doNowOrToday("NOW-1day", "TIMESTAMP"), yesterday));
            Assert.assertTrue("NOW-1days failed to match to yesterday's date", isSameDate(NowAndTodayUtil.doNowOrToday("NOW-1days", "TIMESTAMP"), yesterday));
            Assert.assertTrue("NOW+1d failed to match to tomorrow's date", isSameDate(NowAndTodayUtil.doNowOrToday("now+1d", "TIMESTAMP"), tomorrow));

            Assert.assertTrue("NOW-10h failed to match to 1 minute ago", isSameTime(NowAndTodayUtil.doNowOrToday("now-10h", "TIMESTAMP"), minusTenHours));
            Assert.assertTrue("NOW-10ho failed to match to 1 minute ago", isSameTime(NowAndTodayUtil.doNowOrToday("now-10ho", "TIMESTAMP"), minusTenHours));
            Assert.assertTrue("NOW-10hou failed to match to 1 minute ago", isSameTime(NowAndTodayUtil.doNowOrToday("now-10hou", "TIMESTAMP"), minusTenHours));
            Assert.assertTrue("NOW-10hour failed to match to 1 minute ago", isSameTime(NowAndTodayUtil.doNowOrToday("now-10hour", "TIMESTAMP"), minusTenHours));
            Assert.assertTrue("NOW-10hours failed to match to 1 minute ago", isSameTime(NowAndTodayUtil.doNowOrToday("now-10hours", "TIMESTAMP"), minusTenHours));
            Assert.assertTrue("NOW+10h failed to match to 1 minute ago", isSameTime(NowAndTodayUtil.doNowOrToday("now+10h", "TIMESTAMP"), plusTenHours));

            Assert.assertTrue("NOW-10m failed to match to 1 minute ago", isSameTime(NowAndTodayUtil.doNowOrToday("now-10m", "TIMESTAMP"), minusTenMinute));
            Assert.assertTrue("NOW-10mi failed to match to 1 minute ago", isSameTime(NowAndTodayUtil.doNowOrToday("now-10mi", "TIMESTAMP"), minusTenMinute));
            Assert.assertTrue("NOW-10min failed to match to 1 minute ago", isSameTime(NowAndTodayUtil.doNowOrToday("now-10min", "TIMESTAMP"), minusTenMinute));
            Assert.assertTrue("NOW-10minu failed to match to 1 minute ago", isSameTime(NowAndTodayUtil.doNowOrToday("now-10minu", "TIMESTAMP"), minusTenMinute));
            Assert.assertTrue("NOW-10minut failed to match to 1 minute ago", isSameTime(NowAndTodayUtil.doNowOrToday("now-10minut", "TIMESTAMP"), minusTenMinute));
            Assert.assertTrue("NOW-10minute failed to match to 1 minute ago", isSameTime(NowAndTodayUtil.doNowOrToday("now-10minute", "TIMESTAMP"), minusTenMinute));
            Assert.assertTrue("NOW-10minutes failed to match to 1 minute ago", isSameTime(NowAndTodayUtil.doNowOrToday("now-10minutes", "TIMESTAMP"), minusTenMinute));
            Assert.assertTrue("NOW+10m failed to match to 1 minute ago", isSameTime(NowAndTodayUtil.doNowOrToday("now+10m", "TIMESTAMP"), plusTenMinutes));
            Assert.assertNull(NowAndTodayUtil.doNowOrToday(null, null));
            Assert.assertNull(NowAndTodayUtil.doNowOrToday("2015 01 01", null));
            Assert.assertNull(NowAndTodayUtil.doNowOrToday("NO", null));
        } catch (DateParseException e) {
            fail("Unexpected exception running TIMESTAMP happy path NOW test: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    public void test_doNowOrToday_TODAY_happyPath_TIMESTAMP()
    {
        try {
            Date res = NowAndTodayUtil.doNowOrToday("TODAY", "TIMESTAMP");
            assertTrue(res instanceof java.sql.Timestamp);
        } catch (DateParseException e) {
            fail("Unexpected exception running TIMESTAMP happy path TODAY test: " + e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * Asserts that the year, month, and day of month portions of two dates are the same.
     * @param date1 First date
     * @param date2 Second date (lucky guy)
     * @return true if the year, month, and day are the same, false otherwise.
     */
    private boolean isSameDate(Date date1, Date date2)
    {
        Calendar testC = Calendar.getInstance();
        testC.setTime(date1);

        Calendar correctC = Calendar.getInstance();
        correctC.setTime(date2);

        if (testC.get(Calendar.YEAR) != correctC.get(Calendar.YEAR)) {
            return false;
        }
        if (testC.get(Calendar.MONTH) != correctC.get(Calendar.MONTH)) {
            return false;
        }
        if (testC.get(Calendar.DAY_OF_MONTH) != correctC.get(Calendar.DAY_OF_MONTH)) {
            return false;
        }
        return true;
    }

    /**
     * Asserts that the two dates are within 5 seconds of each other (to allow for flexibility in running the unit test)
     */
    private boolean isSameTime(Date date1, Date date2) {
        if (date1 == null) {
            return false;
        }
        long diffInMillis = abs(date1.getTime() - date2.getTime());
        return diffInMillis < TIME_DIFF_ALLOWED_IN_MILLIS;
    }

    @Test
    public void test_doNowOrToday_errors()
    {
        checkException("TODAY*");
        checkException("TODAY+");
        checkException("TODAY-");
        checkException("TODAY+a");
        checkException("TODAY-a");
    }

    @Test
    public void test_doNowOrToday_invalidColType() throws Exception {
        Date date = NowAndTodayUtil.doNowOrToday("TODAY", "DATE");
        assertTrue(date instanceof java.sql.Date);
        date = NowAndTodayUtil.doNowOrToday("TODAY", "TIME");
        assertTrue(date instanceof java.sql.Time);
        date = NowAndTodayUtil.doNowOrToday("TODAY", "TIMESTAMP");
        assertTrue(date instanceof java.sql.Timestamp);

        try {
            NowAndTodayUtil.doNowOrToday("NOW", null);
            fail("Expected LiquibaseException trying to parse 'NOW' with no column type supplied");
        }
        catch (DateParseException e) {
            // expected behaviour
            assertEquals("Must supply non-null column type when using 'NOW' or 'TODAY' value.", e.getMessage());
        }
        catch (Exception e) {
            fail("Unexpected exception " + e + " while trying to parse 'NOW' with no column type supplied");
        }
        try {
            NowAndTodayUtil.doNowOrToday("TODAY", null);
            fail("Expected LiquibaseException trying to parse 'TODAY' with no column type supplied");
        }
        catch (DateParseException e) {
            // expected behaviour
            assertEquals("Must supply non-null column type when using 'NOW' or 'TODAY' value.", e.getMessage());
        }
        catch (Exception e) {
            fail("Unexpected exception " + e + " while trying to parse 'TODAY' with no column type supplied");
        }

        try {
            NowAndTodayUtil.doNowOrToday("NOW", "BOOLEAN");
            fail("Expected LiquibaseException trying to parse 'NOW' with no column type supplied");
        }
        catch (DateParseException e) {
            // expected behaviour
            assertEquals("Unrecognized colType BOOLEAN when using 'NOW' or 'TODAY' value; expected one of date, time, datetime, or timestamp", e.getMessage());
        }
        catch (Exception e) {
            fail("Unexpected exception " + e + " while trying to parse 'NOW' with BOOLEAN column type supplied");
        }
        try {
            NowAndTodayUtil.doNowOrToday("TODAY", "BOOLEAN");
            fail("Expected LiquibaseException trying to parse 'TODAY' with no column type supplied");
        }
        catch (DateParseException e) {
            // expected behaviour
            assertEquals("Unrecognized colType BOOLEAN when using 'NOW' or 'TODAY' value; expected one of date, time, datetime, or timestamp", e.getMessage());
        }
        catch (Exception e) {
            fail("Unexpected exception " + e + " while trying to parse 'TODAY' with BOOLEAN column type supplied");
        }

        try {
            NowAndTodayUtil.doNowOrToday("NOW", "JUNK");
            fail("Expected LiquibaseException trying to parse 'NOW' with no column type supplied");
        }
        catch (DateParseException e) {
            // expected behaviour
            assertEquals("Unrecognized colType JUNK when using 'NOW' or 'TODAY' value; expected one of date, time, datetime, or timestamp", e.getMessage());
        }
        catch (Exception e) {
            fail("Unexpected exception " + e + " while trying to parse 'NOW' with JUNK column type supplied");
        }
        try {
            NowAndTodayUtil.doNowOrToday("TODAY", "JUNK");
            fail("Expected LiquibaseException trying to parse 'TODAY' with no column type supplied");
        }
        catch (DateParseException e) {
            // expected behaviour
            assertEquals("Unrecognized colType JUNK when using 'NOW' or 'TODAY' value; expected one of date, time, datetime, or timestamp", e.getMessage());
        }
        catch (Exception e) {
            fail("Unexpected exception " + e + " while trying to parse 'TODAY' with JUNK column type supplied");
        }

    }


    private void checkException(String todayValue) {
        try {
            NowAndTodayUtil.doNowOrToday("TODAY*", null);
            fail("Expected exception with invalid 'today' value of '" + todayValue + "'");
        } catch (DateParseException e) {
            // this is the expected exception, so let it pass...
        } catch (Exception e)
        {
            fail("Unexpected exception testing 'doNowOrToday(" + todayValue + ")'");
        }
    }

}
