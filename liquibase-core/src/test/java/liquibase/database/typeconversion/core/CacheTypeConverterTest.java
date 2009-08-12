package liquibase.database.typeconversion.core;

import org.junit.Test;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import liquibase.database.structure.type.DataType;
import liquibase.database.structure.type.UnknownType;

public class CacheTypeConverterTest extends DefaultTypeConverterTest {
    @Test
    public void getFalseBooleanValue() {
        assertEquals("0", new CacheTypeConverter().getFalseBooleanValue());
    }

    @Test
    public void getTrueBooleanValue() {
        assertEquals("1", new CacheTypeConverter().getTrueBooleanValue());
    }
    
    @Test
	public void getDateType() {
		assertTypesEqual(new UnknownType("DATE", false), new CacheTypeConverter().getDateType());
	}

    @Test
	public void getBlobType() {
		assertTypesEqual(new UnknownType("LONGVARBINARY", true), new CacheTypeConverter().getBlobType());
	}

    @Test
	public void getBooleanType() {
		assertTypesEqual(new UnknownType("INTEGER", true), new CacheTypeConverter().getBooleanType());
	}

    @Test
	public void getClobType() {
		assertTypesEqual(new UnknownType("LONGVARCHAR", true), new CacheTypeConverter().getClobType());
	}

    @Test
	public void getCurrencyType() {
		assertTypesEqual(new UnknownType("MONEY", true), new CacheTypeConverter().getCurrencyType());
	}

    @Test
    public void getDateTimeType() {
        assertTypesEqual(new UnknownType("DATETIME", false), new CacheTypeConverter().getDateTimeType());
    }

    @Test
    public void getUUIDType() {
        assertTypesEqual(new UnknownType("CHAR(36)", false), new CacheTypeConverter().getUUIDType());
    }

}
