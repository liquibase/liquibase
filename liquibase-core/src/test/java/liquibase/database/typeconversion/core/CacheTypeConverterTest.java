package liquibase.database.typeconversion.core;

import org.junit.Test;
import org.junit.Assert;
import liquibase.database.typeconversion.DataType;

public class CacheTypeConverterTest extends DefaultTypeConverterTest {
    @Test
    public void getFalseBooleanValue() {
        Assert.assertEquals("0", new CacheTypeConverter().getFalseBooleanValue());
    }

    @Test
    public void getTrueBooleanValue() {
        Assert.assertEquals("1", new CacheTypeConverter().getTrueBooleanValue());
    }
    
    @Test
	public void getDateType() {
		Assert.assertEquals(new DataType("DATE", false), new CacheTypeConverter().getDateType());
	}

    @Test
	public void getBlobType() {
		Assert.assertEquals(new DataType("LONGVARBINARY", true), new CacheTypeConverter().getBlobType());
	}

    @Test
	public void getBooleanType() {
		Assert.assertEquals(new DataType("INTEGER", true), new CacheTypeConverter().getBooleanType());
	}

    @Test
	public void getClobType() {
		Assert.assertEquals(new DataType("LONGVARCHAR", true), new CacheTypeConverter().getClobType());
	}

    @Test
	public void getCurrencyType() {
		Assert.assertEquals(new DataType("MONEY", true), new CacheTypeConverter().getCurrencyType());
	}

    @Test
    public void getDateTimeType() {
        Assert.assertEquals(new DataType("DATETIME", false), new CacheTypeConverter().getDateTimeType());
    }

    @Test
    public void getUUIDType() {
        Assert.assertEquals(new DataType("CHAR(36)", false), new CacheTypeConverter().getUUIDType());
    }

}
