package liquibase.database.typeconversion.core;

import liquibase.database.Database;
import liquibase.database.core.DerbyDatabase;
import liquibase.database.structure.type.BooleanType;
import liquibase.database.structure.type.DateTimeType;
import liquibase.database.structure.type.NumberType;
import liquibase.database.structure.type.TinyIntType;
import liquibase.exception.DatabaseException;

import java.text.ParseException;
import java.sql.Types;

public class DerbyTypeConverter  extends AbstractTypeConverter {

    private BooleanType booleanType;

    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    public boolean supports(Database database) {
        boolean supports = database instanceof DerbyDatabase;
        if (supports) {
            try {
                if (database.getDatabaseMajorVersion() >= 10 && database.getDatabaseMinorVersion() >= 7) {
                    booleanType = new BooleanType("BOOLEAN");
                }
            } catch (DatabaseException e) {
                //can't determine type
            }
        }
        return supports;
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
    public DateTimeType getDateTimeType() {
        return new DateTimeType("TIMESTAMP");
    }

    @Override
    public NumberType getNumberType() {
        return new NumberType("NUMERIC");
    }

    @Override
    public TinyIntType getTinyIntType() {
        return new TinyIntType("SMALLINT");
    }

    @Override
    public BooleanType getBooleanType() {
        if (this.booleanType != null) {
            return booleanType;
        }
        return new BooleanType.NumericBooleanType("SMALLINT");
    }
}
