package liquibase.database.typeconversion;

import liquibase.database.Database;
import liquibase.database.core.CacheDatabase;
import liquibase.database.core.DB2Database;
import liquibase.database.core.DerbyDatabase;
import liquibase.database.core.FirebirdDatabase;
import liquibase.database.core.H2Database;
import liquibase.database.core.HsqlDatabase;
import liquibase.database.core.InformixDatabase;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.MaxDBDatabase;
import liquibase.database.core.MySQLDatabase;
import liquibase.database.core.OracleDatabase;
import liquibase.database.core.PostgresDatabase;
import liquibase.database.core.SQLiteDatabase;
import liquibase.database.core.SybaseASADatabase;
import liquibase.database.core.SybaseDatabase;
import liquibase.database.typeconversion.core.CacheTypeConverter;
import liquibase.database.typeconversion.core.DB2TypeConverter;
import liquibase.database.typeconversion.core.DefaultTypeConverter;
import liquibase.database.typeconversion.core.DerbyTypeConverter;
import liquibase.database.typeconversion.core.FirebirdTypeConverter;
import liquibase.database.typeconversion.core.H2TypeConverter;
import liquibase.database.typeconversion.core.HsqlTypeConverter;
import liquibase.database.typeconversion.core.InformixTypeConverter;
import liquibase.database.typeconversion.core.MSSQLTypeConverter;
import liquibase.database.typeconversion.core.MaxDBTypeConverter;
import liquibase.database.typeconversion.core.MySQLTypeConverter;
import liquibase.database.typeconversion.core.OracleTypeConverter;
import liquibase.database.typeconversion.core.PostgresTypeConverter;
import liquibase.database.typeconversion.core.SQLiteTypeConverter;
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
        if(database instanceof CacheDatabase) {
            return new CacheTypeConverter();
        }
        if(database instanceof DB2Database) {
            return new DB2TypeConverter();
        }
        if(database instanceof DerbyDatabase) {
            return new DerbyTypeConverter();
        }
        if(database instanceof FirebirdDatabase) {
            return new FirebirdTypeConverter();
        }
        if(database instanceof HsqlDatabase) {
            return new HsqlTypeConverter();
        }
        if(database instanceof H2Database) {
            return new H2TypeConverter();
        }
        if(database instanceof InformixDatabase) {
            return new InformixTypeConverter();
        }
        if(database instanceof MaxDBDatabase) {
            return new MaxDBTypeConverter();
        }
        if(database instanceof MSSQLDatabase) {
            return new MSSQLTypeConverter();
        }
        if(database instanceof MySQLDatabase) {
            return new MySQLTypeConverter();
        }
        if(database instanceof OracleDatabase) {
            return new OracleTypeConverter();
        }
        if(database instanceof PostgresDatabase) {
            return new PostgresTypeConverter();
        }
        if(database instanceof SQLiteDatabase) {
            return new SQLiteTypeConverter();
        }
        if(database instanceof SybaseASADatabase) {
            return new SybaseASATypeConverter();
        }
        if(database instanceof SybaseDatabase) {
            return new SybaseTypeConverter();
        }
        return new DefaultTypeConverter();
    }
}
