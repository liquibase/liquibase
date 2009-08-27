package liquibase.database.typeconversion.core;

import org.junit.Test;
import static org.junit.Assert.*;
import liquibase.database.structure.type.CustomType;

public class H2TypeConverterTest extends DefaultTypeConverterTest {
    
    
    @Test
    public void getBlobType() {
        assertEquals("LONGVARBINARY", new H2TypeConverter().getBlobType().toString());
    }

    
    @Test
    public void getBooleanType() {
        assertEquals("BOOLEAN", new H2TypeConverter().getBooleanType().toString());
    }

    
    @Test
    public void getCurrencyType() {
        assertEquals("DECIMAL", new H2TypeConverter().getCurrencyType().toString());
    }

    
    @Test
    public void getUUIDType() {
        assertEquals("VARCHAR(36)", new H2TypeConverter().getUUIDType().toString());
    }

    
    @Test
    public void getClobType() {
        assertEquals("LONGVARCHAR", new H2TypeConverter().getClobType().toString());
    }

    
    @Test
    public void getDateType() {
        assertEquals("DATE", new H2TypeConverter().getDateType().toString());
    }

    
    @Test
    public void getDateTimeType() {
        assertEquals("TIMESTAMP", new H2TypeConverter().getDateTimeType().toString());
    }

    
}
