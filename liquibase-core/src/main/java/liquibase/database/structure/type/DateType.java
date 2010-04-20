package liquibase.database.structure.type;

import liquibase.statement.DatabaseFunction;
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
        } else if (value instanceof DatabaseFunction) {
            return ((DatabaseFunction) value).getValue();
        } else if (value.toString().equals("CURRENT_TIMESTAMP()")) {
              return database.getCurrentDateTimeFunction();
        }
        if (value instanceof java.sql.Timestamp) {
            return database.getDateLiteral(((java.sql.Timestamp) value));
        } else if (value instanceof java.sql.Date) {
            return database.getDateLiteral(((java.sql.Date) value));
        } else if (value instanceof java.sql.Time) {
            return database.getDateLiteral(((java.sql.Time) value));
        } else {
            return database.getDateLiteral(((java.util.Date) value));

        }
    }


}
