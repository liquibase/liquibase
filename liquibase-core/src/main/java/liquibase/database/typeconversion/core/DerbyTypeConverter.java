package liquibase.database.typeconversion.core;

import liquibase.database.Database;
import liquibase.database.core.DerbyDatabase;
import liquibase.database.structure.type.BooleanType;
import liquibase.database.structure.type.DateTimeType;

import java.text.ParseException;
import java.sql.Types;

public class DerbyTypeConverter  extends AbstractTypeConverter {

    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    public boolean supports(Database database) {
        return database instanceof DerbyDatabase;
    }

    @Override
    public Object convertDatabaseValueToObject(Object defaultValue, int dataType, int columnSize, int decimalDigits, Database database) throws ParseException {
        if (defaultValue != null && defaultValue instanceof String) {
            if (dataType == Types.TIMESTAMP) {
                defaultValue = ((String) defaultValue).replaceFirst("^TIMESTAMP\\('", "").replaceFirst("'\\)", "");
            } else if (dataType == Types.DATE) {
                defaultValue = ((String) defaultValue).replaceFirst("^DATE\\('", "").replaceFirst("'\\)", "");
            } else if (dataType == Types.TIME) {
                defaultValue = ((String) defaultValue).replaceFirst("^TIME\\('", "").replaceFirst("'\\)", "");
            }
        }
        return super.convertDatabaseValueToObject(defaultValue, dataType, columnSize, decimalDigits, database);
    }

    @Override
    public BooleanType getBooleanType() {
        return new BooleanType.NumericBooleanType("SMALLINT");
    }

    @Override
    public DateTimeType getDateTimeType() {
        return new DateTimeType("TIMESTAMP");
    }
}
