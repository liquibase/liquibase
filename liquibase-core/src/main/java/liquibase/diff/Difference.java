package liquibase.diff;

import liquibase.serializer.AbstractLiquibaseSerializable;
import liquibase.serializer.LiquibaseSerializable;

public class Difference extends AbstractLiquibaseSerializable implements Comparable, LiquibaseSerializable {
    private String message;
    private String field;
    private Object referenceValue;
    private Object comparedValue;

    public Difference(String field, Object referenceValue, Object comparedValue) {
        this(null, field, referenceValue, comparedValue);
    }

    public Difference(String message, String field, Object referenceValue, Object comparedValue) {
        if (message == null) {
            message = field+" changed from '"+referenceValue+"' to '"+comparedValue+"'";
        }
        this.message = message;
        this.field = field;
        this.referenceValue = referenceValue;
        this.comparedValue = comparedValue;
    }

    @Override
    public String getSerializedObjectName() {
        return "difference";
    }

    @Override
    public String getSerializedObjectNamespace() {
        return null;
    }

    public String getMessage() {
        return message;
    }

    public String getField() {
        return field;
    }

    public Object getReferenceValue() {
        return referenceValue;
    }

    public Object getComparedValue() {
        return comparedValue;
    }

    @Override
    public String toString() {
        return message;
    }

    @Override
    public int compareTo(Object o) {
        return this.getField().compareTo(((Difference) o).getField());
    }
}
