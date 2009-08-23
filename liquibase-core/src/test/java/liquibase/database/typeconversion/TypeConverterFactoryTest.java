package liquibase.database.typeconversion;

import org.junit.Test;
import static org.junit.Assert.*;
import liquibase.database.core.UnsupportedDatabase;
import liquibase.database.core.H2Database;
import liquibase.database.typeconversion.core.DefaultTypeConverter;
import liquibase.database.typeconversion.core.H2TypeConverter;

public class TypeConverterFactoryTest {
    @Test
    public void findTypeConverter() {
        assertTrue(TypeConverterFactory.getInstance().findTypeConverter(new UnsupportedDatabase()) instanceof DefaultTypeConverter);
        assertTrue(TypeConverterFactory.getInstance().findTypeConverter(new H2Database()) instanceof H2TypeConverter);                                                                                                               
     }
}
