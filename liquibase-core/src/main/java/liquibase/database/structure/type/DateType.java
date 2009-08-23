package liquibase.database.structure.type;

import liquibase.statement.ComputedDateValue;
import liquibase.database.Database;

public class DateType extends DataType {

    @Override
    public String getDataTypeName() {
        return "DATE";
    }

    @Override
    public String convertObjectToString(Object value, Database database) {
        if (value == null) {
            return null;
        } else if (value instanceof ComputedDateValue) {
            return ((ComputedDateValue) value).getValue();
        }
        return database.getDateLiteral(((java.sql.Date) value));
    }


}
