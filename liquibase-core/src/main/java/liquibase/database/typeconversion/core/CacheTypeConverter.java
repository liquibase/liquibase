package liquibase.database.typeconversion.core;

import liquibase.database.Database;
import liquibase.database.structure.type.BlobType;
import liquibase.database.structure.type.BooleanType;
import liquibase.database.structure.type.ClobType;
import liquibase.database.structure.type.CurrencyType;

import java.text.ParseException;

public class CacheTypeConverter extends DefaultTypeConverter {

    @Override
    public Object convertDatabaseValueToJavaObject(Object defaultValue, int dataType, int columnSize, int decimalDigits, Database database) throws ParseException {
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
        return super.convertDatabaseValueToJavaObject(defaultValue, dataType, columnSize, decimalDigits, database);

    }

    @Override
    public String getFalseBooleanValue() {
        return "0";
    }

    @Override
    public String getTrueBooleanValue() {
        return "1";
    }

    @Override
    public BlobType getBlobType() {
        return new BlobType() {
            @Override
            public String getDataTypeName() {
                return "LONGVARBINARY";
            }
        };
    }

    @Override
    public BooleanType getBooleanType() {
        return new BooleanType() {
            @Override
            public String getDataTypeName() {
                return "INTEGER";
            }
        };
    }

    @Override
    public ClobType getClobType() {
        return new ClobType() {
            @Override
            public String getDataTypeName() {
                return "LONGVARCHAR";
            }

        };
    }

    @Override
    public CurrencyType getCurrencyType() {
        return new CurrencyType() {
            @Override
            public String getDataTypeName() {
                return "MONEY";
            }
        };
    }
}
