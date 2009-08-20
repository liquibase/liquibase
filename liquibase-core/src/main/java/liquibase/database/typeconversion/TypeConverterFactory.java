package liquibase.database.typeconversion;

import liquibase.database.Database;
import liquibase.database.core.DerbyDatabase;
import liquibase.database.core.InformixDatabase;
import liquibase.database.core.MySQLDatabase;
import liquibase.database.core.OracleDatabase;
import liquibase.database.core.PostgresDatabase;
import liquibase.database.core.SybaseASADatabase;
import liquibase.database.core.SybaseDatabase;
import liquibase.database.typeconversion.core.DefaultTypeConverter;
import liquibase.database.typeconversion.core.DerbyTypeConverter;
import liquibase.database.typeconversion.core.InformixTypeConverter;
import liquibase.database.typeconversion.core.MSSQLTypeConverter;
import liquibase.database.typeconversion.core.OracleTypeConverter;
import liquibase.database.typeconversion.core.PostgresTypeConverter;
import liquibase.database.typeconversion.core.SybaseASATypeConverter;
import liquibase.database.typeconversion.core.SybaseTypeConverter;

public class TypeConverterFactory {
    private static TypeConverterFactory instance;

    public static TypeConverterFactory getInstance() {
        if (instance == null) {
            instance = new TypeConverterFactory();
        }
        return instance;
    }

    public TypeConverter findTypeConverter(Database database) {
        if(database instanceof OracleDatabase) {
            return new OracleTypeConverter();
        }
        if(database instanceof InformixDatabase) {
            return new InformixTypeConverter();
        }
        if(database instanceof PostgresDatabase) {
            return new PostgresTypeConverter();
        }
        if(database instanceof MySQLDatabase) {
            return new MSSQLTypeConverter();
        }
        if(database instanceof SybaseDatabase) {
            return new SybaseTypeConverter();
        }
        if(database instanceof SybaseASADatabase) {
            return new SybaseASATypeConverter();
        }
        if(database instanceof DerbyDatabase) {
            return new DerbyTypeConverter();
        }
        return new DefaultTypeConverter();
    }
}
