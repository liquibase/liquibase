package liquibase.database.typeconversion.core;

import liquibase.database.Database;
import liquibase.database.core.PostgresDatabase;
import liquibase.database.structure.type.BlobType;
import liquibase.database.structure.type.ClobType;
import liquibase.database.structure.type.DateTimeType;

import java.text.ParseException;
import java.sql.Types;

public class PostgresTypeConverter extends AbstractTypeConverter {

    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    public boolean supports(Database database) {
        return database instanceof PostgresDatabase;
    }


    @Override
    public Object convertDatabaseValueToObject(Object defaultValue, int dataType, int columnSize, int decimalDigits, Database database) throws ParseException {
        if (defaultValue != null) {
            if (defaultValue instanceof String) {
                defaultValue = ((String) defaultValue).replaceAll("'::[\\w\\s]+$", "'");

                if (dataType == Types.DATE || dataType == Types.TIME || dataType == Types.TIMESTAMP) {
                    //remove trailing time zone info
                    defaultValue = ((String) defaultValue).replaceFirst("-\\d+$", "");
                }
            }
        }
        return super.convertDatabaseValueToObject(defaultValue, dataType, columnSize, decimalDigits, database);

    }

    @Override
    public String getColumnType(String columnType, Boolean autoIncrement) {
        if (columnType.startsWith("java.sql.Types.VARCHAR")) { //returns "name" for type
            return columnType.replace("java.sql.Types.", "");
        }

        String type = super.getColumnType(columnType, autoIncrement);

        if (type.startsWith("TEXT(")) {
            return getClobType().getDataTypeName();
        } else if (type.toLowerCase().startsWith("float8")) {
            return "FLOAT8";
        } else if (type.toLowerCase().startsWith("float4")) {
            return "FLOAT4";
        }


        if (autoIncrement != null && autoIncrement) {
            if ("integer".equals(type.toLowerCase())) {
                return "serial";
            } else if ("bigint".equals(type.toLowerCase()) || "bigserial".equals(type.toLowerCase())) {
                return "bigserial";
            } else {
                // Unknown integer type, default to "serial"
                return "serial";
            }
        }

        return type;
    }


    @Override
    protected Object convertToCorrectObjectType(String value, int dataType, int columnSize, int decimalDigits, Database database) throws ParseException {
        Object returnValue = super.convertToCorrectObjectType(value, dataType, columnSize, decimalDigits, database);
        if (returnValue != null && returnValue instanceof String) {
            if (((String) returnValue).startsWith("NULL::")) {
                return null;
            }
        }
        return returnValue;
    }

    @Override
    public ClobType getClobType() {
        return new ClobType("TEXT");
    }

    @Override
    public BlobType getBlobType() {
        return new BlobType("BYTEA");
    }

    @Override
    public DateTimeType getDateTimeType() {
        return new DateTimeType("TIMESTAMP WITH TIME ZONE");
    }
}
