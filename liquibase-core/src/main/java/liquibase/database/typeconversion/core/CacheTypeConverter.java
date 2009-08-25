package liquibase.database.typeconversion.core;

import liquibase.database.Database;
import liquibase.database.core.CacheDatabase;
import liquibase.database.structure.type.BlobType;
import liquibase.database.structure.type.BooleanType;
import liquibase.database.structure.type.ClobType;
import liquibase.database.structure.type.CurrencyType;

import java.text.ParseException;

public class CacheTypeConverter extends AbstractTypeConverter {

    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    public boolean supports(Database database) {
        return database instanceof CacheDatabase;
    }

    @Override
    public Object convertDatabaseValueToObject(Object defaultValue, int dataType, int columnSize, int decimalDigits, Database database) throws ParseException {
        if (defaultValue != null) {
            if (defaultValue instanceof String) {
                String stringDefaultValue = (String) defaultValue;
                if (stringDefaultValue.charAt(0) == '"' && stringDefaultValue.charAt(stringDefaultValue.length() - 1) == '"') {
                    defaultValue = stringDefaultValue.substring(1, stringDefaultValue.length() - 1);
                } else if (stringDefaultValue.startsWith("$")) {
                    defaultValue = "OBJECTSCRIPT '" + defaultValue + "'";
                }
            }
        }
        return super.convertDatabaseValueToObject(defaultValue, dataType, columnSize, decimalDigits, database);

    }

    @Override
    public BlobType getBlobType() {
        return new BlobType("LONGVARBINARY");
    }

    @Override
    public BooleanType getBooleanType() {
        return new BooleanType.NumericBooleanType();
    }

    @Override
    public ClobType getClobType() {
        return new ClobType("LONGVARCHAR");
    }

    @Override
    public CurrencyType getCurrencyType() {
        return new CurrencyType("MONEY");
    }

}
