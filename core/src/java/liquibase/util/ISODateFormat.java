package liquibase.util;

import liquibase.database.sql.ComputedDateValue;

import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ISODateFormat {

    private SimpleDateFormat dateTimeFormat = new SimpleDateFormat(DATE_TIME_FORMAT_STRING);
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private static final String DATE_TIME_FORMAT_STRING = "yyyy-MM-dd'T'HH:mm:ss";


    public String format(java.sql.Date date) {
        return dateFormat.format(date);
    }

    public String format(java.sql.Time date) {
        return timeFormat.format(date);
    }

    public String format(java.sql.Timestamp date) {
        return dateTimeFormat.format(date);
    }

    public String format(Date date) {
        if (date instanceof ComputedDateValue) {
            return date.toString();
        } else if (date instanceof java.sql.Date) {
            return format(((java.sql.Date) date));
        } else if (date instanceof Time) {
            return format(((java.sql.Time) date));
        } else if (date instanceof java.sql.Timestamp) {
            return format(((java.sql.Timestamp) date));
        } else {
            throw new RuntimeException("Unknown type: "+date.getClass().getName());
        }
    }

    public Date parse(String dateAsString) throws ParseException {
        if (dateAsString.length() == DATE_TIME_FORMAT_STRING.length()-2) { //subtract 2 to not count the 's
            return new java.sql.Timestamp(dateTimeFormat.parse(dateAsString).getTime());
        } else {
            if (dateAsString.indexOf(":") > 0) {
                return new java.sql.Time(timeFormat.parse(dateAsString).getTime());
            } else {
                return new java.sql.Date(dateFormat.parse(dateAsString).getTime());
            }
        }
    }
}
