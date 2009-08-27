package liquibase.database.structure.type;

import liquibase.statement.ComputedDateValue;
import liquibase.database.Database;

public class DateType extends DataType {

    public DateType() {
        super("DATE",0,0);
    }

    public DateType(String dataTypeName) {
        super(dataTypeName,0,0);
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
