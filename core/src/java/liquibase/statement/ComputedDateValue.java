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


    @Override
    public long getTime() {
        throw new RuntimeException("Date computed by database");
    }

    @Override
    public String toString() {
        return getValue();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ComputedDateValue) {
            return this.toString().equals(obj.toString());
        } else {
            return super.equals(obj);
        }
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }
}
