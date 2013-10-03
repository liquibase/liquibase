package liquibase.exception;

import java.text.ParseException;

public class DateParseException extends ParseException {
    public DateParseException(String s) {
        super(s, 0);
    }
}
