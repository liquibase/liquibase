package liquibase.database.typeconversion.core;

import liquibase.database.Database;
import liquibase.database.core.DB2Database;
import liquibase.database.structure.type.BooleanType;
import liquibase.database.structure.type.CurrencyType;
import liquibase.database.structure.type.DateTimeType;
import liquibase.database.structure.type.NumberType;

import java.text.ParseException;
import java.sql.Types;

public class DB2TypeConverter  extends AbstractTypeConverter {

    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    public boolean supports(Database database) {
        return database instanceof DB2Database;
    }

    @Override
    public Object convertDatabaseValueToObject(Object defaultValue, int dataType, int columnSize, int decimalDigits, Database database) throws ParseException {
        if (defaultValue != null && defaultValue instanceof String) {
            if (dataType == Types.TIMESTAMP) {
                defaultValue = ((String) defaultValue).replaceFirst("^\"SYSIBM\".\"TIMESTAMP\"\\('", "").replaceFirst("'\\)", "");
            } else if (dataType == Types.TIME) {
                defaultValue = ((String) defaultValue).replaceFirst("^\"SYSIBM\".\"TIME\"\\('", "").replaceFirst("'\\)", "");
            } else if (dataType == Types.DATE) {
                defaultValue = ((String) defaultValue).replaceFirst("^\"SYSIBM\".\"DATE\"\\('", "").replaceFirst("'\\)", "");
            }
        }
        return super.convertDatabaseValueToObject(defaultValue, dataType, columnSize, decimalDigits, database);
    }

    @Override
    public BooleanType getBooleanType() {
        return new BooleanType.NumericBooleanType("SMALLINT");
    }

    @Override
    public CurrencyType getCurrencyType() {
        return new CurrencyType("DECIMAL(19,4)");
    }

    @Override
    public DateTimeType getDateTimeType() {
        return new DateTimeType("TIMESTAMP");
    }

    @Override
    public NumberType getNumberType() {
        return new NumberType("DECFLOAT(16)");
    }
    
}
