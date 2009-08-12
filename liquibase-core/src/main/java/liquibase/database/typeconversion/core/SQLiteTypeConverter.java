package liquibase.database.typeconversion.core;

import liquibase.database.structure.type.ClobType;
import liquibase.database.structure.type.CurrencyType;
import liquibase.database.structure.type.DateTimeType;

import java.util.Locale;

public class SQLiteTypeConverter extends DefaultTypeConverter {

    @Override
    public String getColumnType(String columnType, Boolean autoIncrement) {
        String type;
        if (columnType.equals("INTEGER") ||
                columnType.toLowerCase(Locale.ENGLISH).contains("int") ||
                columnType.toLowerCase(Locale.ENGLISH).contains("bit")) {
            type = "INTEGER";
        } else if (columnType.equals("TEXT") ||
                columnType.toLowerCase(Locale.ENGLISH).contains("uuid") ||
                columnType.toLowerCase(Locale.ENGLISH).contains("uniqueidentifier") ||

                columnType.toLowerCase(Locale.ENGLISH).equals("uniqueidentifier") ||
                columnType.toLowerCase(Locale.ENGLISH).equals("datetime") ||
                columnType.toLowerCase(Locale.ENGLISH).contains("timestamp") ||
                columnType.toLowerCase(Locale.ENGLISH).contains("char") ||
                columnType.toLowerCase(Locale.ENGLISH).contains("clob") ||
                columnType.toLowerCase(Locale.ENGLISH).contains("text")) {
            type = "TEXT";
        } else if (columnType.equals("REAL") ||
                columnType.toLowerCase(Locale.ENGLISH).contains("float")) {
            type = "REAL";
        } else if (columnType.toLowerCase(Locale.ENGLISH).contains("blob") ||
                columnType.toLowerCase(Locale.ENGLISH).contains("binary")) {
            type = "BLOB";
        } else if (columnType.toLowerCase(Locale.ENGLISH).contains("boolean") ||
                columnType.toLowerCase(Locale.ENGLISH).contains("binary")) {
            type = "BOOLEAN";
        } else {
            type = super.getColumnType(columnType, autoIncrement);
        }
        return type;
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
    public ClobType getClobType() {
        return new ClobType() {
            @Override
            public String getDataTypeName() {
                return "TEXT";
            }
        };
    }

    @Override
    public CurrencyType getCurrencyType() {
        return new CurrencyType() {
            @Override
            public String getDataTypeName() {
                return "REAL";
            }
        };
    }

    @Override
    public DateTimeType getDateTimeType() {
        return new DateTimeType() {
            @Override
            public String getDataTypeName() {
                return "TEXT";
            }
        };
    }
}
