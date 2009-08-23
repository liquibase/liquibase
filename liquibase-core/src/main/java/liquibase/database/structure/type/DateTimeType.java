package liquibase.database.structure.type;

import liquibase.statement.ComputedDateValue;
import liquibase.database.Database;

public class DateTimeType extends DataType {
    @Override
    public String getDataTypeName() {
        return "DATETIME";
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
