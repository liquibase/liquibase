package liquibase.util;

import liquibase.exception.DateParseException;
import org.junit.Assert;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TodayUtilTest {

    @Test
    public void test_isTodayFormat()
    {
        assertTrue(TodayUtil.isTodayFormat("today"));
        assertTrue(TodayUtil.isTodayFormat("Today"));
        assertTrue(TodayUtil.isTodayFormat("TODAY"));
        assertTrue(TodayUtil.isTodayFormat("today+1"));
        assertTrue(TodayUtil.isTodayFormat("Today-2"));
        assertTrue(TodayUtil.isTodayFormat("TODAY+5"));

        assertFalse(TodayUtil.isTodayFormat(null));
        assertFalse(TodayUtil.isTodayFormat(""));
        assertFalse(TodayUtil.isTodayFormat("Toda"));
        assertFalse(TodayUtil.isTodayFormat("2015-01-01"));
    }

    @Test
    public void test_doToday_happyPath()
    {
        Calendar c = Calendar.getInstance();
        Date today = c.getTime();
        c.add(Calendar.DATE, -1);
        Date yesterday = c.getTime();
        c = Calendar.getInstance();
        c.add(Calendar.DATE, 1);
        Date tomorrow = c.getTime();

        try {
            Assert.assertTrue("TODAY failed to match to today's date", isSameDate(TodayUtil.doToday("TODAY"), today));
            Assert.assertTrue("TODAY-1 failed to match to yesterday's date", isSameDate(TodayUtil.doToday("TODAY-1"), yesterday));
            Assert.assertTrue("TODAY+1 failed to match to tomorrow's date", isSameDate(TodayUtil.doToday("TODAY+1"), tomorrow));
            Assert.assertNull(TodayUtil.doToday(null));
            Assert.assertNull(TodayUtil.doToday("2015 01 01"));
            Assert.assertNull(TodayUtil.doToday("TODA"));
        } catch (DateParseException e) {
            fail("Unexpected exception running happy path TODAY test: " + e.getMessage());
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

    @Test
    public void test_doToday_errors()
    {
        checkException("TODAY*");
        checkException("TODAY+");
        checkException("TODAY-");
        checkException("TODAY+a");
        checkException("TODAY-a");
    }

    private void checkException(String todayValue) {
        try {
            TodayUtil.doToday("TODAY*");
            fail("Expected exception with invalid 'today' value of '" + todayValue + "'");
        } catch (DateParseException e) {
            // this is the expected exception, so let it pass...
        } catch (Exception e) {
            fail("Unexpected exception testing 'doToday(" + todayValue + ")'");
        }
    }
}
