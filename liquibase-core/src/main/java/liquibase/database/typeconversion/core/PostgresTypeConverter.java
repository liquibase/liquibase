package liquibase.database.typeconversion.core;

import liquibase.database.Database;
import liquibase.database.structure.type.BlobType;
import liquibase.database.structure.type.ClobType;
import liquibase.database.structure.type.DateTimeType;

import java.text.ParseException;
import java.sql.Types;

public class PostgresTypeConverter extends DefaultTypeConverter {

    @Override
    public Object convertDatabaseValueToJavaObject(Object defaultValue, int dataType, int columnSize, int decimalDigits, Database database) throws ParseException {
        if (defaultValue != null) {
            if (defaultValue instanceof String) {
                defaultValue = ((String) defaultValue).replaceAll("'::[\\w\\s]+$", "'");

                if (dataType == Types.DATE || dataType == Types.TIME || dataType == Types.TIMESTAMP) {
                    //remove trailing time zone info
                    defaultValue = ((String) defaultValue).replaceFirst("-\\d+$", "");
                }
            }
        }
        return super.convertDatabaseValueToJavaObject(defaultValue, dataType, columnSize, decimalDigits, database);

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
    protected Object convertToCorrectJavaType(String value, int dataType, int columnSize, int decimalDigits, Database database) throws ParseException {
        Object returnValue = super.convertToCorrectJavaType(value, dataType, columnSize, decimalDigits, database);
        if (returnValue != null && returnValue instanceof String) {
            if (((String) returnValue).startsWith("NULL::")) {
                return null;
            }
        }
        return returnValue;
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
                return "BYTEA";
            }
        };
    }

    @Override
    public DateTimeType getDateTimeType() {
        return new DateTimeType() {
            @Override
            public String getDataTypeName() {
                return "TIMESTAMP WITH TIME ZONE";
            }
        };
    }

    /**
     * @see http://www.postgresql.org/docs/8.4/static/datatype-boolean.html
     */
    public String getFalseBooleanValue() {
        return "FALSE";
    }

    /**
     * @see http://www.postgresql.org/docs/8.4/static/datatype-boolean.html
     */
    public String getTrueBooleanValue() {
        return "TRUE";
    }
}
