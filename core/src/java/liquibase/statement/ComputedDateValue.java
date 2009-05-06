package liquibase.statement;

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

    public boolean equals(Object obj) {
        if (obj instanceof ComputedDateValue) {
            return this.toString().equals(obj.toString());
        } else {
            return super.equals(obj);
        }
    }

    public int hashCode() {
        return this.toString().hashCode();
    }
}
