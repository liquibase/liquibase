package liquibase.database.typeconversion.core;

import org.junit.Test;
import org.junit.Assert;
import liquibase.database.typeconversion.DataType;

public class H2TypeConverterTest extends DefaultTypeConverterTest {
    
    
    @Test
    public void getBlobType() {
        Assert.assertEquals(new DataType("LONGVARBINARY", true), new H2TypeConverter().getBlobType());
    }

    
    @Test
    public void getBooleanType() {
        Assert.assertEquals(new DataType("BOOLEAN", false), new H2TypeConverter().getBooleanType());
    }

    
    @Test
    public void getCurrencyType() {
        Assert.assertEquals(new DataType("DECIMAL", true), new H2TypeConverter().getCurrencyType());
    }

    
    @Test
    public void getUUIDType() {
        Assert.assertEquals(new DataType("VARCHAR(36)", false), new H2TypeConverter().getUUIDType());
    }

    
    @Test
    public void getClobType() {
        Assert.assertEquals(new DataType("LONGVARCHAR", true), new H2TypeConverter().getClobType());
    }

    
    @Test
    public void getDateType() {
        Assert.assertEquals(new DataType("DATE", false), new H2TypeConverter().getDateType());
    }

    
    @Test
    public void getDateTimeType() {
        Assert.assertEquals(new DataType("TIMESTAMP", false), new H2TypeConverter().getDateTimeType());
    }

    
}
