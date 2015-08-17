package liquibase.structure;

import java.util.Date;

public class DateWithTimezone extends Date {

    private String timezone;

    public DateWithTimezone() {
    }

    public DateWithTimezone(long date) {
        super(date);
    }

    public DateWithTimezone(Date date, String timezone) {
        super(date.getTime());
        this.timezone = timezone;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }
}
