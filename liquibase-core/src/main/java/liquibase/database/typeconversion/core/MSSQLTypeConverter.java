package liquibase.database.typeconversion.core;

import liquibase.database.Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.structure.type.*;

import java.text.ParseException;

public class MSSQLTypeConverter extends AbstractTypeConverter {

    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    public boolean supports(Database database) {
        return database instanceof MSSQLDatabase;
    }


    @Override
    public Object convertDatabaseValueToObject(Object defaultValue, int dataType, int columnSize, int decimalDigits, Database database) throws ParseException {
        if (defaultValue == null) {
            return null;
        }

        if (defaultValue instanceof String) {
            if (((String) defaultValue).startsWith("('")) {
                defaultValue = ((String) defaultValue).replaceFirst("^\\('", "").replaceFirst("'\\)$", "");
            } else if (((String) defaultValue).startsWith("((")) {
                defaultValue = ((String) defaultValue).replaceFirst("^\\(\\(", "").replaceFirst("\\)\\)$", "");
            }
        }

        defaultValue = super.convertDatabaseValueToObject(defaultValue, dataType, columnSize, decimalDigits, database);

        return defaultValue;
    }


    @Override
    public String getColumnType(String columnType, Boolean autoIncrement) {
        String type = super.getColumnType(columnType, autoIncrement);
        if (autoIncrement != null && autoIncrement) {
            type = type.replaceFirst(" identity$", "");
        }
        return type;
    }

    @Override
    public String getTrueBooleanValue() {
        return "1";
    }

    @Override
    public String getFalseBooleanValue() {
        return "0";
    }

    @Override
    public DateType getDateType() {
        return new DateType() {
            @Override
            public String getDataTypeName() {
                return "SMALLDATETIME";
            }
        };
    }

    @Override
    public BooleanType getBooleanType() {
        return new BooleanType() {
            @Override
            public String getDataTypeName() {
                return "BIT";
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

    @Override
    public UUIDType getUUIDType() {
        return new UUIDType() {
            @Override
            public String getDataTypeName() {
                return "UNIQUEIDENTIFIER";
            }
        };
    }

    @Override
    public ClobType getClobType() {
        return new ClobType() {
            @Override
            public String getDataTypeName() {
                return "TEXT";
            }
        };
    }

    @Override
    public BlobType getBlobType() {
        return new BlobType() {
            @Override
            public String getDataTypeName() {
                return "IMAGE";
            }
        };
    }


}
