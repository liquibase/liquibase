package liquibase.database.typeconversion;

import liquibase.database.Database;
import liquibase.database.structure.type.*;
import liquibase.change.ColumnConfig;
import liquibase.servicelocator.PrioritizedService;

import java.text.ParseException;

public interface TypeConverter extends PrioritizedService {

    int getPriority();

    boolean supports(Database database);

    Object convertDatabaseValueToObject(Object defaultValue, int dataType, int firstParameter, int secondParameter, Database database) throws ParseException;

    DataType getDataType(Object object);

    String getColumnType(String columnType, Boolean autoIncrement);

    String getColumnType(ColumnConfig columnConfig);

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
