package liquibase.database.typeconversion.core;

import org.junit.Test;
import static org.junit.Assert.*;
import liquibase.database.structure.type.CustomType;

public class MSSQLTypeConverterTest extends DefaultTypeConverterTest{
    
    @Test
    public void getBlobType() {
        assertEquals("VARBINARY(MAX)", new MSSQLTypeConverter().getBlobType().toString());
    }
    
    @Test
    public void getBooleanType() {
        assertEquals("BIT", new MSSQLTypeConverter().getBooleanType().toString());
    }

    
    @Test
    public void getCurrencyType() {
        assertEquals("MONEY", new MSSQLTypeConverter().getCurrencyType().toString());
    }

    
    @Test
    public void getUUIDType() {
        assertEquals("UNIQUEIDENTIFIER", new MSSQLTypeConverter().getUUIDType().toString());
    }

    
    @Test
    public void getClobType() {
        assertEquals("NVARCHAR(MAX)", new MSSQLTypeConverter().getClobType().toString());
    }

    
    @Test
    public void getDateType() {
        assertEquals("SMALLDATETIME", new MSSQLTypeConverter().getDateType().toString());
    }

    
    @Test
    public void getDateTimeType() {
        assertEquals("DATETIME", new MSSQLTypeConverter().getDateTimeType().toString());
    }

    
    @Test
    public void getNumberType() {
        assertEquals("NUMERIC", new MSSQLTypeConverter().getNumberType().toString());
    }
}
