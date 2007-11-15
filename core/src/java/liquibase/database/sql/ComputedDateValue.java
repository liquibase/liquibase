package liquibase.database.sql;

import java.util.Date;

public class ComputedDateValue extends Date {

    private String value;

    public ComputedDateValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }


    public long getTime() {
        throw new RuntimeException("Date computed by database");
    }

    public String toString() {
        return getValue();
    }
}
