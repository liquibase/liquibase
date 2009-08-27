package liquibase.database.typeconversion.core;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import liquibase.database.structure.type.CustomType;

public class CacheTypeConverterTest extends DefaultTypeConverterTest {
    @Test
    public void getFalseBooleanValue() {
        assertEquals("0", new CacheTypeConverter().getBooleanType().getFalseBooleanValue());
    }

    @Test
    public void getTrueBooleanValue() {
        assertEquals("1", new CacheTypeConverter().getBooleanType().getTrueBooleanValue());
    }
    
    @Test
	public void getDateType() {
		assertEquals("DATE", new CacheTypeConverter().getDateType().toString());
	}

    @Test
	public void getBlobType() {
		assertEquals("LONGVARBINARY", new CacheTypeConverter().getBlobType().toString());
	}

    @Test
	public void getBooleanType() {
		assertEquals("INT", new CacheTypeConverter().getBooleanType().toString());
	}

    @Test
	public void getClobType() {
		assertEquals("LONGVARCHAR", new CacheTypeConverter().getClobType().toString());
	}

    @Test
	public void getCurrencyType() {
		assertEquals("MONEY", new CacheTypeConverter().getCurrencyType().toString());
	}

    @Test
    public void getDateTimeType() {
        assertEquals("DATETIME", new CacheTypeConverter().getDateTimeType().toString());
    }

    @Test
    public void getUUIDType() {
        assertEquals("CHAR(36)", new CacheTypeConverter().getUUIDType().toString());
    }

}
