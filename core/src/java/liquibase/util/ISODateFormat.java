package liquibase.util;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ISODateFormat extends DateFormat {

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd'T'HH:MM:ss");


    public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition fieldPosition) {
        return dateFormat.format(date, toAppendTo, fieldPosition);
    }

    public Date parse(String source, ParsePosition pos) {
        return dateFormat.parse(source, pos);
    }
}
