package liquibase.database.structure.type;

import liquibase.statement.ComputedDateValue;
import liquibase.database.Database;

public class DateTimeType extends DataType {
    public DateTimeType() {
        super("DATETIME",0,0);
    }

    public DateTimeType(String dataTypeName) {
        super(dataTypeName,0,0);
    }

    @Override
    public boolean getSupportsPrecision() {
        return true;
    }

    @Override
    public String convertObjectToString(Object value, Database database) {
        if (value == null) {
            return null;
        }  else if (value instanceof ComputedDateValue) {
            return ((ComputedDateValue) value).getValue();
        }

        return database.getDateTimeLiteral(((java.sql.Timestamp) value));
    }
}
