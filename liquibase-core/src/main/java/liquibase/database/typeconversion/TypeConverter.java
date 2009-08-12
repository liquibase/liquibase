package liquibase.database.typeconversion;

import liquibase.database.typeconversion.DataType;
import liquibase.database.Database;
import liquibase.change.ColumnConfig;

import java.text.ParseException;

public interface TypeConverter {

    Object convertDatabaseValueToJavaObject(Object defaultValue, int dataType, int columnSize, int decimalDigits, Database database) throws ParseException;

    String convertJavaObjectToString(Object value, Database database);

    int getDatabaseType(int type);
    
    String getColumnType(String columnType, Boolean autoIncrement);

    String getColumnType(ColumnConfig columnConfig);

    String getFalseBooleanValue();

    String getTrueBooleanValue();

    DataType getCharType();

    DataType getVarcharType();

    DataType getBooleanType();

    DataType getCurrencyType();

    DataType getUUIDType();

    DataType getClobType();

    DataType getBlobType();

    DataType getDateType();

    DataType getFloatType();

    DataType getDoubleType();

    DataType getIntType();

    DataType getTinyIntType();

    DataType getDateTimeType();

    DataType getTimeType();

    DataType getBigIntType();

}
