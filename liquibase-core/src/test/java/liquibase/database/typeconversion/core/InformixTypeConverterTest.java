package liquibase.database.typeconversion.core;

import static org.junit.Assert.*;
import liquibase.database.typeconversion.TypeConverter;
import liquibase.database.structure.type.DataType;

@SuppressWarnings({"UnusedAssignment"})
public class InformixTypeConverterTest extends DefaultTypeConverterTest {

    public void testGetColumnType() {
        DataType type;

        TypeConverter typeConverter = new InformixTypeConverter();
        type = typeConverter.getDataType("int", true);
        assertEquals("SERIAL", type.toString());

        type = typeConverter.getDataType("INT", true);
        assertEquals("SERIAL", type.toString());

        type = typeConverter.getDataType("integer", true);
        assertEquals("SERIAL", type.toString());

        type = typeConverter.getDataType("INTEGER", true);
        assertEquals("SERIAL", type.toString());

        type = typeConverter.getDataType("BIGINT", true);
        assertEquals("SERIAL8", type.toString());

        type = typeConverter.getDataType("bigint", true);
        assertEquals("SERIAL8", type.toString());

        type = typeConverter.getDataType("int8", true);
        assertEquals("SERIAL8", type.toString());

        try {
            type = typeConverter.getDataType("integ", true);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Unknown autoincrement type: integ", e.getMessage());
        }

        try {
            type = typeConverter.getDataType("varchar(10)", true);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Unknown autoincrement type: varchar(10)", e.getMessage());
        }
    }
}
