package liquibase.database;

import static org.junit.Assert.*;

import org.junit.Test;

public class CacheDatabaseTest extends AbstractDatabaseTest{

	public CacheDatabaseTest() {
        super(new CacheDatabase());
    }
	
	protected String getProductNameString() {
	      return "Cache";
	    }
	
	@Test
	public void supportsSequences() {
		assertFalse(database.supportsSequences());
	}

	@Test
	public void getFalseBooleanValue() {
		assertEquals("0", database.getFalseBooleanValue());
	}

	@Test
	public void getTrueBooleanValue() {
		assertEquals("1", database.getTrueBooleanValue());
	}

	@Test
	public void getLineComment() {
		assertEquals("--", database.getLineComment());
	}

	@Test
	public void getDefaultDriver() {
		assertEquals("com.intersys.jdbc.CacheDriver", 
				database.getDefaultDriver("jdbc:Cache://127.0.0.1:56773/TESTMIGRATE"));
	}

	@Test
	public void getProductName() {
		assertEquals(getProductNameString(), database.getProductName());
	}

	@Test
	public void getTypeName() {
		assertEquals("cache", database.getTypeName());
	}

	@Test
	public void getDateType() {
		assertEquals("DATE", database.getDateType());
	}

	@Test
	public void getBlobType() {
		assertEquals("LONGVARBINARY", database.getBlobType());
		
	}

	@Test
	public void getBooleanType() {
		assertEquals("INTEGER", database.getBooleanType());
	}

	@Test
	public void getClobType() {
		assertEquals("LONGVARCHAR", database.getClobType());
	}

	@Test
	public void getCurrencyType() {
		assertEquals("MONEY", database.getCurrencyType());
	}

	@Test
	public void getCurrentDateTimeFunction() {
		assertEquals("SYSDATE", database.getCurrentDateTimeFunction());
	}

	@Test
	public void getDateTimeType() {
		assertEquals("DATETIME", database.getDateTimeType());
	}

	@Test
	public void getUUIDType() {
		assertEquals("RAW", database.getUUIDType());
	}

	@Test
	public void supportsInitiallyDeferrableColumns() {
		assertFalse(database.supportsInitiallyDeferrableColumns());
	}
}
