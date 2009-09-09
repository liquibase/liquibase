package liquibase.database.typeconversion;

import liquibase.database.DatabaseFactory;
import liquibase.database.Database;

public class TypeConversionReport {
    public static void main(String[] args) {
        for (Database database : DatabaseFactory.getInstance().getImplementedDatabases()) {
            TypeConverter typeConverter = TypeConverterFactory.getInstance().findTypeConverter(database);
            System.out.println("Database: "+database.getTypeName()+", Converter: "+typeConverter.getClass().getName());
            System.out.println("'bigint'="+typeConverter.getBigIntType());
            System.out.println("'blob'="+typeConverter.getBlobType());
            System.out.println("'boolean'="+typeConverter.getBooleanType());
            System.out.println("'char'="+typeConverter.getCharType());
            System.out.println("'clob'="+typeConverter.getClobType());
            System.out.println("'currency'="+typeConverter.getCurrencyType());
            System.out.println("'datetime'="+typeConverter.getDateTimeType());
            System.out.println("'double'="+typeConverter.getDoubleType());
            System.out.println("'float'="+typeConverter.getFloatType());
            System.out.println("'int'="+typeConverter.getIntType());
            System.out.println("'time'="+typeConverter.getTimeType());
            System.out.println("'tinyint'="+typeConverter.getTinyIntType());
            System.out.println("'uuid'="+typeConverter.getUUIDType());
            System.out.println("'varchar'="+typeConverter.getVarcharType());

            System.out.println("");
        }
    }
}
