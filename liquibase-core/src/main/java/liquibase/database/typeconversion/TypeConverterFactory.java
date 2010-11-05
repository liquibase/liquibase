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
import liquibase.servicelocator.ServiceLocator;
import liquibase.exception.UnexpectedLiquibaseException;

import java.util.*;

public class TypeConverterFactory {
    private static TypeConverterFactory instance;
    private Set<TypeConverter> allConverters;

    public static TypeConverterFactory getInstance() {
        if (instance == null) {
            instance = new TypeConverterFactory();
        }
        return instance;
    }

    private TypeConverterFactory() {
        allConverters = new HashSet<TypeConverter>();
        try {
            for (Class<? extends TypeConverter> converterClass : ServiceLocator.getInstance().findClasses(TypeConverter.class)) {
                register(converterClass.newInstance());
            }
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    public TypeConverter findTypeConverter(Database database) {
        SortedSet<TypeConverter> converters = new TreeSet<TypeConverter>(new Comparator<TypeConverter>() {
            public int compare(TypeConverter o1, TypeConverter o2) {
                return Integer.valueOf(o1.getPriority()).compareTo(o2.getPriority());
            }
        });

        //noinspection unchecked

        for (TypeConverter converter : allConverters) {
            if (converter.supports(database)) {
                converters.add(converter);
            }
        }

        return converters.last();
    }

    public void register(TypeConverter typeConverter) {
        allConverters.add(typeConverter);
    }

    public void register(Class<? extends TypeConverter> typeConverterClass) {
        try {
            allConverters.add(typeConverterClass.newInstance());
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }
}
