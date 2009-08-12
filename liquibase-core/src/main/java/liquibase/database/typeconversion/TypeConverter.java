package liquibase.database.typeconversion;

import liquibase.database.Database;
import liquibase.database.structure.type.*;
import liquibase.change.ColumnConfig;

import java.text.ParseException;

public interface TypeConverter {

    Object convertDatabaseValueToJavaObject(Object defaultValue, int dataType, int columnSize, int decimalDigits, Database database) throws ParseException;

    String convertJavaObjectToString(Object value, Database database);

    String getColumnType(String columnType, Boolean autoIncrement);

    String getColumnType(ColumnConfig columnConfig);

    String getFalseBooleanValue();

    String getTrueBooleanValue();

    CharType getCharType();

    VarcharType getVarcharType();

    BooleanType getBooleanType();

    CurrencyType getCurrencyType();

    UUIDType getUUIDType();

    ClobType getClobType();

    BlobType getBlobType();

    DateType getDateType();

    FloatType getFloatType();

    DoubleType getDoubleType();

    IntType getIntType();

    TinyIntType getTinyIntType();

    DateTimeType getDateTimeType();

    TimeType getTimeType();

    BigIntType getBigIntType();

}
