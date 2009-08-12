package liquibase.database.typeconversion.core;

import org.junit.Test;
import org.junit.Assert;
import liquibase.database.typeconversion.DataType;

public class MySQLTypeConverterTest extends DefaultTypeConverterTest {
    
    @Test
    public void getBlobType() {
        Assert.assertEquals(new DataType("BLOB", true), new MySQLTypeConverter().getBlobType());
    }

    
    @Test
    public void getBooleanType() {
        Assert.assertEquals(new DataType("TINYINT(1)", false), new MySQLTypeConverter().getBooleanType());
    }

    
    @Test
    public void getCurrencyType() {
        Assert.assertEquals(new DataType("DECIMAL", true), new MySQLTypeConverter().getCurrencyType());
    }

    
    @Test
    public void getUUIDType() {
        Assert.assertEquals(new DataType("CHAR(36)", false), new MySQLTypeConverter().getUUIDType());
    }

    
    @Test
    public void getClobType() {
        Assert.assertEquals(new DataType("TEXT", true), new MySQLTypeConverter().getClobType());
    }

    
    @Test
    public void getDateType() {
        Assert.assertEquals(new DataType("DATE", false), new MySQLTypeConverter().getDateType());
    }

    
    @Test
    public void getDateTimeType() {
        Assert.assertEquals(new DataType("DATETIME", false), new MySQLTypeConverter().getDateTimeType());
    }
    
}
