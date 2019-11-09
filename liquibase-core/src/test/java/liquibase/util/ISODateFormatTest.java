package liquibase.util;

import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;

public class ISODateFormatTest {

    private ISODateFormat dateFormat = new ISODateFormat();

    @Test
    public void isoDateFormatWithNoLeadingZeroFractions() throws Exception {
        Date date = dateFormat.parse("2012-09-12T09:47:54.664");
        assertEquals("2012-09-12T09:47:54.664", dateFormat.format(date));
    }

    @Test
    public void isoDateFormatWithLeadingZeroFractions() throws Exception {
        Date date = dateFormat.parse("2011-04-21T10:13:40.044");
        assertEquals("2011-04-21T10:13:40.044", dateFormat.format(date));
    }

    @Test
    public void isoDateFormatWithLeadingNoFractions() throws Exception {
        Date date = dateFormat.parse("2011-04-21T10:13:40");
        assertEquals("2011-04-21T10:13:40", dateFormat.format(date));
    }

    @Test
    public void isoDateFormatWithLeadingFractions() throws Exception {
        Date date = dateFormat.parse("2011-04-21T10:13:40.12");
        assertEquals("2011-04-21T10:13:40.12", dateFormat.format(date));
    }

    @Test
    public void isoDateFormatWithLeadingNanoFractions() throws Exception {
        Date date = dateFormat.parse("2011-04-21T10:13:40.01234567");
        assertEquals("2011-04-21T10:13:40.01234567", dateFormat.format(date));
    }
}
