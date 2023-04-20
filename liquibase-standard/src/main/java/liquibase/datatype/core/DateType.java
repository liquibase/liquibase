package liquibase.datatype.core;

import liquibase.change.core.LoadDataChange;
import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.datatype.DataTypeInfo;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.LiquibaseDataType;
import liquibase.statement.DatabaseFunction;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

@DataTypeInfo(name="date", aliases = {"java.sql.Types.DATE", "java.sql.Date"}, minParameters = 0, maxParameters = 0, priority = LiquibaseDataType.PRIORITY_DEFAULT)
public class DateType extends LiquibaseDataType {
    @Override
    public DatabaseDataType toDatabaseDataType(Database database) {
        if (database instanceof MSSQLDatabase) {
            return new DatabaseDataType(database.escapeDataTypeName("date"));
        }
        return new DatabaseDataType(getName());
    }

    @Override
    public LoadDataChange.LOAD_DATA_TYPE getLoadTypeName() {
        return LoadDataChange.LOAD_DATA_TYPE.DATE;
    }

    @Override
    public String objectToSql(Object value, Database database) {
        if ((value == null) || "null".equals(value.toString().toLowerCase(Locale.US))) {
            return null;
        } else if (value instanceof DatabaseFunction) {
            return database.generateDatabaseFunctionValue((DatabaseFunction) value);
        } else if ("CURRENT_TIMESTAMP()".equals(value.toString())) {
              return database.getCurrentDateTimeFunction();
        } else if (value instanceof java.sql.Timestamp) {
            return database.getDateLiteral(((java.sql.Timestamp) value));
        } else if (value instanceof java.sql.Date) {
            return database.getDateLiteral(((java.sql.Date) value));
        } else if (value instanceof java.sql.Time) {
            return database.getDateLiteral(((java.sql.Time) value));
        } else if (value instanceof java.util.Date) {
            return database.getDateLiteral(((java.util.Date) value));
        } else {
            return "'"+((String) value).replaceAll("'","''")+"'";
        }
    }


    @Override
    public Object sqlToObject(String value, Database database) {
        if (database instanceof AbstractDb2Database) {
            return value.replaceFirst("^\"SYSIBM\".\"DATE\"\\('", "").replaceFirst("'\\)", "");
        }
        if (database instanceof DerbyDatabase) {
            return value.replaceFirst("^DATE\\('", "").replaceFirst("'\\)", "");
        }

        if (zeroTime(value)) {
            return value;
        }
        try {
            DateFormat dateFormat = getDateFormat(database);

            if ((database instanceof OracleDatabase) && value.matches("to_date\\('\\d+\\-\\d+\\-\\d+', " +
                "'YYYY\\-MM\\-DD'\\)")) {
                dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                value = value.replaceFirst(".*?'", "").replaceFirst("',.*","");
            }

            return new java.sql.Date(dateFormat.parse(value.trim()).getTime());
        } catch (ParseException e) {
            return new DatabaseFunction(value);
        }
    }

    private boolean zeroTime(String stringVal) {
        return "".equals(stringVal.replace("-", "").replace(":", "").replace(" ", "").replace("0", ""));
    }

    protected DateFormat getDateFormat(Database database) {
        if (database instanceof OracleDatabase) {
            return new SimpleDateFormat("dd-MMM-yy");
        } else {
            return new SimpleDateFormat("yyyy-MM-dd");
        }
    }
}
