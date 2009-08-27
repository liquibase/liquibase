package liquibase.database.typeconversion.core;

import liquibase.database.structure.type.*;
import liquibase.database.Database;
import liquibase.database.core.SQLiteDatabase;

import java.util.Locale;

public class SQLiteTypeConverter extends AbstractTypeConverter {

    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    public boolean supports(Database database) {
        return database instanceof SQLiteDatabase;
    }


    @Override
    public DataType getDataType(String columnTypeString, Boolean autoIncrement) {
        DataType type;
        if (columnTypeString.equals("INTEGER") ||
                columnTypeString.toLowerCase(Locale.ENGLISH).contains("int") ||
                columnTypeString.toLowerCase(Locale.ENGLISH).contains("bit")) {
            type = new IntType("INTEGER");
        } else if (columnTypeString.equals("TEXT") ||
                columnTypeString.toLowerCase(Locale.ENGLISH).contains("uuid") ||
                columnTypeString.toLowerCase(Locale.ENGLISH).contains("uniqueidentifier") ||
                columnTypeString.toLowerCase(Locale.ENGLISH).equals("uniqueidentifier") ||
                columnTypeString.toLowerCase(Locale.ENGLISH).equals("datetime") ||
                columnTypeString.toLowerCase(Locale.ENGLISH).contains("timestamp") ||
                columnTypeString.toLowerCase(Locale.ENGLISH).contains("char") ||
                columnTypeString.toLowerCase(Locale.ENGLISH).contains("clob") ||
                columnTypeString.toLowerCase(Locale.ENGLISH).contains("text")) {
            type = new CustomType("TEXT",0,0);
        } else if (columnTypeString.equals("REAL") ||
                columnTypeString.toLowerCase(Locale.ENGLISH).contains("float")) {
            type = new FloatType("REAL");
        } else if (columnTypeString.toLowerCase(Locale.ENGLISH).contains("blob") ||
                columnTypeString.toLowerCase(Locale.ENGLISH).contains("binary")) {
            type = new BlobType("BLOB");
        } else if (columnTypeString.toLowerCase(Locale.ENGLISH).contains("boolean") ||
                columnTypeString.toLowerCase(Locale.ENGLISH).contains("binary")) {
            type = new BooleanType("BOOLEAN");
        } else {
            type = super.getDataType(columnTypeString, autoIncrement);
        }
        return type;
    }

    @Override
    public BooleanType getBooleanType() {
        return new BooleanType() {
            @Override
            public String getFalseBooleanValue() {
                return "0";
            }

            @Override
            public String getTrueBooleanValue() {
                return "1";
            }
        };
    }

    @Override
    public ClobType getClobType() {
        return new ClobType("TEXT");
    }

    @Override
    public CurrencyType getCurrencyType() {
        return new CurrencyType("REAL");
    }

    @Override
    public DateTimeType getDateTimeType() {
        return new DateTimeType("TEXT");
    }
}
