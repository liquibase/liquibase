package liquibase.database.typeconversion.core;

import liquibase.database.structure.type.CustomType;
import org.junit.Test;
import static org.junit.Assert.*;

public class MySQLTypeConverterTest extends DefaultTypeConverterTest {
    
    @Test
    public void getBlobType() {
        assertEquals("BLOB", new MySQLTypeConverter().getBlobType().toString());
    }

    
    @Test
    public void getBooleanType() {
        assertEquals("TINYINT(1)", new MySQLTypeConverter().getBooleanType().toString());
    }

    
    @Test
    public void getCurrencyType() {
        assertEquals("DECIMAL", new MySQLTypeConverter().getCurrencyType().toString());
    }

    
    @Test
    public void getUUIDType() {
        assertEquals("CHAR(36)", new MySQLTypeConverter().getUUIDType().toString());
    }

    
    @Test
    public void getClobType() {
        assertEquals("LONGTEXT", new MySQLTypeConverter().getClobType().toString());
    }

    
    @Test
    public void getDateType() {
        assertEquals("DATE", new MySQLTypeConverter().getDateType().toString());
    }

    
    @Test
    public void getDateTimeType() {
        assertEquals("DATETIME", new MySQLTypeConverter().getDateTimeType().toString());
    }
    
}
