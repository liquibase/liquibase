package liquibase.database.typeconversion.core;

import static org.junit.Assert.*;
import liquibase.database.typeconversion.TypeConverter;
import liquibase.database.core.InformixDatabase;

public class InformixTypeConverterTest extends DefaultTypeConverterTest {

    public void testGetColumnType() {
        String type;

        TypeConverter typeConverter = new InformixTypeConverter();
        type = typeConverter.getColumnType("int", true);
        assertEquals("SERIAL", type);

        type = typeConverter.getColumnType("INT", true);
        assertEquals("SERIAL", type);

        type = typeConverter.getColumnType("integer", true);
        assertEquals("SERIAL", type);

        type = typeConverter.getColumnType("INTEGER", true);
        assertEquals("SERIAL", type);

        type = typeConverter.getColumnType("BIGINT", true);
        assertEquals("SERIAL8", type);

        type = typeConverter.getColumnType("bigint", true);
        assertEquals("SERIAL8", type);

        type = typeConverter.getColumnType("int8", true);
        assertEquals("SERIAL8", type);

        try {
            type = typeConverter.getColumnType("integ", true);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Unknown autoincrement type: integ", e.getMessage());
        }

        try {
            type = typeConverter.getColumnType("varchar(10)", true);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Unknown autoincrement type: varchar(10)", e.getMessage());
        }
    }

    public void testConvertJavaObjectToStringWithBoolean() {
        String s;

        TypeConverter typeConverter = new InformixTypeConverter();


        s = typeConverter.convertJavaObjectToString(Boolean.TRUE, new InformixDatabase());
        assertEquals("'t'", s);

        s = typeConverter.convertJavaObjectToString(Boolean.FALSE, new InformixDatabase());
        assertEquals("'f'", s);
    }

}
