package liquibase.database.typeconversion.core;

import liquibase.database.Database;
import liquibase.database.core.PostgresDatabase;
import liquibase.database.structure.type.*;

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
    public DataType getDataType(String columnTypeString, Boolean autoIncrement) {
        DataType type = super.getDataType(columnTypeString, autoIncrement);

        if (type.getDataTypeName().toLowerCase().contains("text")) {
            type = getClobType();
        } else if (type.getDataTypeName().toLowerCase().contains("blob")) {
            type = getBlobType();
        } else if (type.getDataTypeName().toLowerCase().startsWith("float8")) {
            type.setDataTypeName("FLOAT8");
        } else if (type.getDataTypeName().toLowerCase().startsWith("float4")) {
            type.setDataTypeName("FLOAT4");
        }


        if (autoIncrement != null && autoIncrement) {
            if ("integer".equals(type.getDataTypeName().toLowerCase())) {
                type.setDataTypeName("serial");
            } else if ("bigint".equals(type.getDataTypeName().toLowerCase()) || "bigserial".equals(type.getDataTypeName().toLowerCase())) {
                type.setDataTypeName("bigserial");
            } else {
                // Unknown integer type, default to "serial"
                type.setDataTypeName("serial");
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

    @Override
    public NumberType getNumberType() {
        return new NumberType("NUMERIC");
    }

    @Override
    public TinyIntType getTinyIntType() {
        return new TinyIntType("SMALLINT");
    }

    @Override
    public DoubleType getDoubleType() {
        return new DoubleType("DOUBLE PRECISION");
    }
}
