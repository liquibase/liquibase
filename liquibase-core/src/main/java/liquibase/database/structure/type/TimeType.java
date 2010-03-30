package liquibase.database.structure.type;

import liquibase.statement.DatabaseFunction;
import liquibase.database.Database;

public class TimeType  extends DataType {

    public TimeType() {
        super("TIME",0,0);
    }

    public TimeType(String dataTypeName) {
        super(dataTypeName,0,0);
    }

    @Override
    public String convertObjectToString(Object value, Database database) {
        if (value == null) {
            return null;
        }  else if (value instanceof DatabaseFunction) {
            return ((DatabaseFunction) value).getValue();
        } else if (value.toString().equals("CURRENT_TIMESTAMP()")) {
              return database.getCurrentDateTimeFunction();
        }
        
          return database.getTimeLiteral(((java.sql.Time) value));
    }


}
