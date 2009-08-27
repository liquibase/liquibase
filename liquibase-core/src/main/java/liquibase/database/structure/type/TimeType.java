package liquibase.database.structure.type;

import liquibase.statement.ComputedDateValue;
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
        }  else if (value instanceof ComputedDateValue) {
            return ((ComputedDateValue) value).getValue();
        }        
        return database.getTimeLiteral(((java.sql.Time) value));
    }


}
