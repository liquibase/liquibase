package liquibase.database.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import org.junit.Test;
import org.junit.Assert;
import liquibase.database.core.CacheDatabase;
import liquibase.database.AbstractDatabaseTest;
import liquibase.database.DataType;

public class CacheDatabaseTest extends AbstractDatabaseTest {

	public CacheDatabaseTest() throws Exception {
        super(new CacheDatabase());
    }
	
	@Override
    protected String getProductNameString() {
	      return "Cache";
	    }
	
	@Test
	public void supportsSequences() {
		assertFalse(database.supportsSequences());
	}

	@Test
	public void getFalseBooleanValue() {
		Assert.assertEquals("0", database.getFalseBooleanValue());
	}

	@Test
	public void getTrueBooleanValue() {
		Assert.assertEquals("1", database.getTrueBooleanValue());
	}

	@Test
	public void getLineComment() {
		Assert.assertEquals("--", database.getLineComment());
	}

	@Test
	public void getDefaultDriver() {
		Assert.assertEquals("com.intersys.jdbc.CacheDriver",
				database.getDefaultDriver("jdbc:Cache://127.0.0.1:56773/TESTMIGRATE"));
	}

	@Test
	public void getTypeName() {
		Assert.assertEquals("cache", database.getTypeName());
	}

	@Override
    @Test
	public void getDateType() {
		Assert.assertEquals(new DataType("DATE", false), database.getDateType());
	}

	@Override
    @Test
	public void getBlobType() {
		Assert.assertEquals(new DataType("LONGVARBINARY", true), database.getBlobType());
	}

	@Override
    @Test
	public void getBooleanType() {
		Assert.assertEquals(new DataType("INTEGER", true), database.getBooleanType());
	}

	@Override
    @Test
	public void getClobType() {
		Assert.assertEquals(new DataType("LONGVARCHAR", true), database.getClobType());
	}

	@Override
    @Test
	public void getCurrencyType() {
		Assert.assertEquals(new DataType("MONEY", true), database.getCurrencyType());
	}

	@Override
    @Test
	public void getCurrentDateTimeFunction() {
		Assert.assertEquals("SYSDATE", database.getCurrentDateTimeFunction());
	}

	@Override
    @Test
	public void getDateTimeType() {
		Assert.assertEquals(new DataType("DATETIME", false), database.getDateTimeType());
	}

	@Override
    @Test
	public void getUUIDType() {
		Assert.assertEquals(new DataType("CHAR(36)", false), database.getUUIDType());
	}

	@Override
    @Test
	public void supportsInitiallyDeferrableColumns() {
		assertFalse(database.supportsInitiallyDeferrableColumns());
	}
}
