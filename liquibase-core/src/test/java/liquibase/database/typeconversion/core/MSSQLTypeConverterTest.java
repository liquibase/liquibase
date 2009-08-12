package liquibase.database.typeconversion.core;

import org.junit.Test;
import org.junit.Assert;
import liquibase.database.typeconversion.DataType;

public class MSSQLTypeConverterTest extends DefaultTypeConverterTest{
    
    @Test
    public void getBlobType() {
        Assert.assertEquals(new DataType("IMAGE", true), new MSSQLTypeConverter().getBlobType());
    }
    
    @Test
    public void getBooleanType() {
        Assert.assertEquals(new DataType("BIT", false), new MSSQLTypeConverter().getBooleanType());
    }

    
    @Test
    public void getCurrencyType() {
        Assert.assertEquals(new DataType("MONEY", false), new MSSQLTypeConverter().getCurrencyType());
    }

    
    @Test
    public void getUUIDType() {
        Assert.assertEquals(new DataType("UNIQUEIDENTIFIER", false), new MSSQLTypeConverter().getUUIDType());
    }

    
    @Test
    public void getClobType() {
        Assert.assertEquals(new DataType("TEXT", true), new MSSQLTypeConverter().getClobType());
    }

    
    @Test
    public void getDateType() {
        Assert.assertEquals(new DataType("SMALLDATETIME", false), new MSSQLTypeConverter().getDateType());
    }

    
    @Test
    public void getDateTimeType() {
        Assert.assertEquals(new DataType("DATETIME", false), new MSSQLTypeConverter().getDateTimeType());
    }

    
}
