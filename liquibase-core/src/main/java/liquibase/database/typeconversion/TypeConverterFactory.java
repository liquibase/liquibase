package liquibase.database.typeconversion;

import liquibase.database.Database;
import liquibase.database.typeconversion.core.DefaultTypeConverter;

public class TypeConverterFactory {
    private static TypeConverterFactory instance;

    public static TypeConverterFactory getInstance() {
        if (instance == null) {
            instance = new TypeConverterFactory();
        }
        return instance;
    }

    public TypeConverter findTypeConverter(Database database) {
        return new DefaultTypeConverter();
    }
}
