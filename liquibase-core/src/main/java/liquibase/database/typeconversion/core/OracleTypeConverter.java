package liquibase.database.typeconversion.core;

import liquibase.database.typeconversion.DataType;
import liquibase.database.Database;

import java.text.ParseException;
import java.sql.Types;

public class OracleTypeConverter extends DefaultTypeConverter {

    private static final DataType BOOLEAN_TYPE = new DataType("NUMBER(1)", false);
    private static final DataType CURRENCY_TYPE = new DataType("NUMBER(15, 2)", false);
    private static final DataType UUID_TYPE = new DataType("RAW(16)", false);
    private static final DataType CLOB_TYPE = new DataType("CLOB", false);
    private static final DataType BLOB_TYPE = new DataType("BLOB", false);
    private static final DataType DATETIME_TYPE = new DataType("TIMESTAMP", true);
    private static final DataType DATE_TYPE = new DataType("DATE", false);
    private static final DataType BIGINT_TYPE = new DataType("NUMBER(19,0)", false);

    @Override
    public Object convertDatabaseValueToJavaObject(Object defaultValue, int dataType, int columnSize, int decimalDigits, Database database) throws ParseException {
        if (defaultValue != null) {
            if (defaultValue instanceof String) {
                if (dataType == Types.DATE || dataType == Types.TIME || dataType == Types.TIMESTAMP) {
                    if (((String) defaultValue).indexOf("YYYY-MM-DD HH") > 0) {
                        defaultValue = ((String) defaultValue).replaceFirst("^to_date\\('","").replaceFirst("', 'YYYY-MM-DD HH24:MI:SS'\\)$","");
                    } else if (((String) defaultValue).indexOf("YYYY-MM-DD") > 0) {
                        defaultValue = ((String) defaultValue).replaceFirst("^to_date\\('","").replaceFirst("', 'YYYY-MM-DD'\\)$","");
                    } else {
                        defaultValue = ((String) defaultValue).replaceFirst("^to_date\\('","").replaceFirst("', 'HH24:MI:SS'\\)$","");
                    }
                }
                defaultValue = ((String) defaultValue).replaceFirst("'\\s*$", "'"); //sometimes oracle adds an extra space after the trailing ' (see http://sourceforge.net/tracker/index.php?func=detail&aid=1824663&group_id=187970&atid=923443).
            }
        }
        return super.convertDatabaseValueToJavaObject(defaultValue, dataType, columnSize, decimalDigits, database);
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
    public DataType getBooleanType() {
        return BOOLEAN_TYPE;
    }

    @Override
    public DataType getCurrencyType() {
        return CURRENCY_TYPE;
    }

    @Override
    public DataType getUUIDType() {
        return UUID_TYPE;
    }

    @Override
    public DataType getClobType() {
        return CLOB_TYPE;
    }

    @Override
    public DataType getBlobType() {
        return BLOB_TYPE;
    }

    @Override
    public DataType getDateTimeType() {
        return DATETIME_TYPE;
    }


    @Override
    public DataType getDateType() {
        return DATE_TYPE;
    }

    @Override
    public DataType getTimeType() {
        return DATE_TYPE;
    }

    @Override
    public DataType getBigIntType() {
        return BIGINT_TYPE;
    }

}
